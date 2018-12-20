package kidridicarus.agent.SMB.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agencydirector.BasicInputs;
import kidridicarus.agency.ADefFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.PipeWarp;
import kidridicarus.agent.bodies.SMB.player.MarioBody;
import kidridicarus.agent.bodies.SMB.player.MarioBody.MarioBodyState;
import kidridicarus.agent.general.GuideSpawner;
import kidridicarus.agent.general.Room;
import kidridicarus.agent.sprites.SMB.player.MarioSprite;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.SMBInfo.PowerupType;
import kidridicarus.info.UInfo;

/*
 * TODO:
 * -the body physics code has only been tested with non-moving surfaces, needs to be tested with moving platforms
 * -mario will sometimes not go down a pipe warp even though he is in the right place on top of the pipe - fix this
 */
public class Mario extends Agent {
	public enum MarioState { PLAY, FIREBALL, DEAD, END1_SLIDE, END2_WAIT1, END3_WAIT2, END4_FALL, END5_BRAKE,
		END6_RUN, END99, PIPE_ENTRYH, PIPE_EXITH, PIPE_ENTRYV, PIPE_EXITV };

	private static final float LEVEL_MAX_TIME = 300f;

	private static final float DMG_INVINCIBLE_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float TIME_PER_FIREBALL = 0.5f;
	private static final float POWERSTAR_TIME = 15f;
	private static final float FLAG_SLIDE_VELOCITY = -0.9f;
	private static final float END_FLAGWAIT = 0.4f;
	private static final float END_BRAKETIME = 0.02f;
	private static final Vector2 FLAG_JUMPOFF_VEL = new Vector2(1.0f, 1.0f);

	private static float PIPE_WARPHEIGHT = UInfo.P2M(32);
	private static final float PIPE_WARPWIDTH = UInfo.P2M(16);
	private static final float PIPE_WARPENTRYTIME = 0.7f;

	public enum MarioPowerState { SMALL, BIG, FIRE };

	private MarioSprite marioSprite;
	private MarioBody mariobody;

	private MarioPowerState curPowerState;

	private boolean marioIsDead;
	private boolean wantsToRunOnPrevUpdate;
	private boolean isDmgInvincible;
	private float dmgInvincibleTime;
	private float fireballTimer;
	private float powerStarTimer;
	private Vector2 marioSpriteOffset;
	private PowerupType receivedPowerup;
	private GuideSpawner exitingSpawnpoint;
	private PointAmount consecBouncePoints;

	private MarioState curState;
	private float stateTimer;

	private float levelTimeRemaining;
	private int extraLives;
	private int coinTotal;
	private int pointTotal;

	private BasicInputs frameInputs;

	public Mario(Agency agency, AgentDef adef) {
		super(agency, adef);

		marioIsDead = false;
		wantsToRunOnPrevUpdate = false;
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		fireballTimer = TIME_PER_FIREBALL * 2f;
		powerStarTimer = 0f;
		marioSpriteOffset = new Vector2(0f, 0f);

		curPowerState = MarioPowerState.SMALL;

		receivedPowerup = PowerupType.NONE;

		exitingSpawnpoint = null;

		levelTimeRemaining = LEVEL_MAX_TIME;

		frameInputs = new BasicInputs();

		curState = MarioState.PLAY;
		stateTimer = 0f;

		extraLives = 2;
		coinTotal = pointTotal = 0;
		consecBouncePoints = PointAmount.ZERO;

		// physic
		mariobody = new MarioBody(this, agency, adef.bounds.getCenter(new Vector2()), true, false);

		// graphic
		marioSprite = new MarioSprite(agency.getEncapTexAtlas(), adef.bounds.getCenter(new Vector2()),
				MarioBodyState.STAND, curPowerState, mariobody.isFacingRight());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.TOP);
	}

	@Override
	public void update(float delta) {
		MarioBodyState bodyState;
		MarioState nextState;
		boolean isStarPowered;

		levelTimeRemaining -= delta;

		// reset consecutive bounce points
		if(mariobody.isOnGround())
			consecBouncePoints = PointAmount.ZERO;

		bodyState = MarioBodyState.STAND;
		nextState = processMarioState(delta, frameInputs);
		// mario special states (e.g. end level script) override regular body states (e.g. mario use powerup) 
		if(nextState == MarioState.PLAY || nextState == MarioState.FIREBALL) {
			processDamage(delta);
			processPowerups();
			bodyState = mariobody.update(delta, frameInputs, curPowerState);
		}

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;

		isStarPowered = false;
		if(powerStarTimer > 0f) {
			isStarPowered = true;
			powerStarTimer -= delta;
			// restart regular music when powerstar powerup finishes
			if(powerStarTimer <= 0f)
				agency.startMusic();
		}

		marioSprite.update(delta, mariobody.getPosition().cpy().add(marioSpriteOffset), curState, bodyState,
				curPowerState, mariobody.isFacingRight(), isDmgInvincible, isStarPowered, mariobody.isBigBody());

		wantsToRunOnPrevUpdate = frameInputs.wantsToRun;
		
		frameInputs.clear();
	}

	// Process the body and return a character state based on the findings.
	private MarioState processMarioState(float delta, BasicInputs bi) {
		// scripted level end sequence
		if(mariobody.getLevelEndContacted() != null) {
			mariobody.zeroVelocity(true, true);
			return MarioState.END99;
		}
		// scripted dead sequence
		else if(marioIsDead) {
			if(curState != MarioState.DEAD) {
				agency.stopMusic();
				agency.playSound(AudioInfo.SOUND_MARIODIE);

				mariobody.disableContacts();

				mariobody.setVelocity(0f, 0f);
				mariobody.applyImpulse(new Vector2(0, 4f));
			}
			// make sure mario doesn't move left or right while dead
			mariobody.zeroVelocity(true, false);
			return MarioState.DEAD;
		}
		// scripted flagpole level end sequence
		else if(mariobody.getFlagpoleContacted() != null) {
			// scripted level end sequence using curCharState and stateTimer
			switch(curState) {
				case END1_SLIDE:
					// switch sides if necessary when hit ground
					if(mariobody.isOnGround())
						return MarioState.END2_WAIT1;
					// sliding down
					else {
						mariobody.setVelocity(0f, FLAG_SLIDE_VELOCITY);
						return MarioState.END1_SLIDE;
					}
				case END2_WAIT1:
					if(mariobody.getFlagpoleContacted().isAtBottom()) {
						mariobody.setFacingRight(false);
						// if mario is on left side of flagpole, move him to right side
						if(mariobody.getFlagpoleContacted().getPosition().x > mariobody.getPosition().x) {
							mariobody.setPosAndVel(mariobody.getPosition().cpy().add(2f *
									(mariobody.getFlagpoleContacted().getPosition().x -
									mariobody.getPosition().x), 0f),
									new Vector2(0f, 0f));
						}
						return MarioState.END3_WAIT2;
					}
					else
						return MarioState.END2_WAIT1;
				case END3_WAIT2:
					if(stateTimer > END_FLAGWAIT) {
						// switch to first walk frame and push mario to right
						mariobody.enableGravity();
						mariobody.applyImpulse(FLAG_JUMPOFF_VEL);
						return MarioState.END4_FALL;
					}
					else
						return MarioState.END3_WAIT2;
				case END4_FALL:
					if(mariobody.isOnGround())
						return MarioState.END5_BRAKE;
					else
						return MarioState.END4_FALL;
				case END5_BRAKE:
					if(stateTimer > END_BRAKETIME) {
						mariobody.setFacingRight(true);
						agency.startSinglePlayMusic(AudioInfo.MUSIC_LEVELEND);
						mariobody.resetFlagpoleContacted();
						return MarioState.END6_RUN;
					}
					else
						return MarioState.END5_BRAKE;
				case END6_RUN:
 					mariobody.moveBodyLeftRight(true, false);
					return MarioState.END6_RUN;
				// first frame of level end state
				default:
					mariobody.getFlagpoleContacted().startDrop();
					mariobody.disableGravity();
					mariobody.zeroVelocity(true, true);

					agency.stopMusic();
					agency.playSound(AudioInfo.SOUND_FLAGPOLE);

					return MarioState.END1_SLIDE;
			}
		}
		// scripted pipe entrance
		else if(mariobody.getPipeToEnter() != null) {
			switch(curState) {
				// continuing pipe entry
				case PIPE_ENTRYH:
				case PIPE_ENTRYV:
					marioSpriteOffset.set(getPipeEntrySpriteEndOffset(mariobody.getPipeToEnter()));
					if(stateTimer < PIPE_WARPENTRYTIME)
						marioSpriteOffset.scl(stateTimer / PIPE_WARPENTRYTIME);
					return curState;
				// first frame of pipe entry
				default:
					agency.playSound(AudioInfo.SOUND_POWERDOWN);

					mariobody.disableContacts();

					mariobody.disableGravity();
					mariobody.zeroVelocity(true, true);
					if(mariobody.getPipeToEnter().getDirection().isHorizontal())
						return MarioState.PIPE_ENTRYH;
					else
						return MarioState.PIPE_ENTRYV;
			}
		}
		// scripted spawnpoint exit
		else if(exitingSpawnpoint != null) {
			switch(curState) {
				// continuing pipe exit
				case PIPE_EXITH:
				case PIPE_EXITV:
					marioSpriteOffset.set(getSpawnExitSpriteBeginOffset());
					if(stateTimer > PIPE_WARPENTRYTIME) {
						exitingSpawnpoint = null;
						marioSpriteOffset.set(0f, 0f);
						return MarioState.PLAY;
					}
					else {
						marioSpriteOffset.scl((PIPE_WARPENTRYTIME - stateTimer) / PIPE_WARPENTRYTIME);
						return curState;
					}
				// first frame of pipe exit
				default:
					if(exitingSpawnpoint.getDirection().isHorizontal())
						return MarioState.PIPE_EXITH;
					else
						return MarioState.PIPE_EXITV;
			}
		}
		// otherwise the player has control, because no script is runnning
		else {
			if(processFireball(delta, bi))
				return MarioState.FIREBALL;
			else
				return MarioState.PLAY;
		}
	}

	// mario can shoot fireballs two at a time, but must wait if his "fireball timer" runs low
	private boolean processFireball(float delta, BasicInputs bi) {
		if(curState != MarioState.PLAY)
			return false;

		fireballTimer += delta;
		if(fireballTimer > TIME_PER_FIREBALL)
			fireballTimer = TIME_PER_FIREBALL;

		// fire a ball?
		if(curPowerState == MarioPowerState.FIRE && bi.wantsToRun && !wantsToRunOnPrevUpdate && fireballTimer > 0f) {
			fireballTimer -= TIME_PER_FIREBALL;
			throwFireball();
			return true;
		}

		return false;
	}

	private void throwFireball() {
		Vector2 offset;
		if(mariobody.isFacingRight())
			offset = mariobody.getPosition().cpy().add(FIREBALL_OFFSET, 0f);
		else
			offset = mariobody.getPosition().cpy().add(-FIREBALL_OFFSET, 0f);

		agency.createAgent(ADefFactory.makeMarioFireballDef(offset, mariobody.isFacingRight(), this));
		agency.playSound(AudioInfo.SOUND_FIREBALL);
	}

	private void processPowerups() {
		// apply powerup if received
		switch(receivedPowerup) {
			case MUSH1UP:
				agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P1UP, false,
						mariobody.getPosition(), UInfo.P2M(16), this));
				break;
			case MUSHROOM:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mariobody.setBodyPosVelAndSize(mariobody.getPosition().add(0f, UInfo.P2M(8f)),
							mariobody.getVelocity(), true);
					agency.playSound(AudioInfo.SOUND_POWERUP_USE);
				}
				agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P1000, false,
						mariobody.getPosition(), UInfo.P2M(16), this));
				break;
			case FIREFLOWER:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mariobody.setBodyPosVelAndSize(mariobody.getPosition().add(0f, UInfo.P2M(8f)),
							mariobody.getVelocity(), true);
					agency.playSound(AudioInfo.SOUND_POWERUP_USE);
				}
				else if(curPowerState == MarioPowerState.BIG) {
					curPowerState = MarioPowerState.FIRE;
					agency.playSound(AudioInfo.SOUND_POWERUP_USE);
				}
				agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P1000, false,
						mariobody.getPosition(), UInfo.P2M(16), this));
				break;
			case POWERSTAR:
				powerStarTimer = POWERSTAR_TIME;
				agency.stopMusic();
				agency.playSound(AudioInfo.SOUND_POWERUP_USE);
				agency.startSinglePlayMusic(AudioInfo.MUSIC_STARPOWER);
				agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P1000, false,
						mariobody.getPosition(), UInfo.P2M(16), this));
				break;
			case NONE:
				break;
		}

		receivedPowerup = PowerupType.NONE;
	}

	private void processDamage(float delta) {
		if(dmgInvincibleTime > 0f)
			dmgInvincibleTime -= delta;
		else if(isDmgInvincible)
			endDmgInvincibility();

		// apply damage if received
		if(mariobody.getAndResetTakeDamage()) {
			// fire mario becomes small mario
			// big mario becomes small mario
			if(curPowerState == MarioPowerState.FIRE || curPowerState == MarioPowerState.BIG) {
				curPowerState = MarioPowerState.SMALL;
				if(mariobody.isDucking())
					mariobody.setBodyPosVelAndSize(mariobody.getPosition(), mariobody.getVelocity(), false);
				else {
					mariobody.setBodyPosVelAndSize(mariobody.getPosition().sub(0f, UInfo.P2M(8f)),
							mariobody.getVelocity(), false);
				}
				
				startDmgInvincibility();
				agency.playSound(AudioInfo.SOUND_POWERDOWN);
			}
			// die if small and not invincible
			else
				marioIsDead = true;
		}
	}

	private void startDmgInvincibility() {
		isDmgInvincible = true;
		dmgInvincibleTime = DMG_INVINCIBLE_TIME;

		mariobody.disableAgentContact();
	}

	private void endDmgInvincibility() {
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;

		mariobody.enableAgentContact();
	}

	public void applyPowerup(PowerupType powerup) {
		// TODO: check if already received powerup, and check for rank
		receivedPowerup = powerup;
	}

	@Override
	public void draw(Batch batch) {
		if(mariobody.getLevelEndContacted() == null)
			marioSprite.draw(batch);
	}

	public void setFrameInputs(BasicInputs bi) {
		frameInputs = bi.cpy();
	}

	public void respawn(GuideSpawner sp) {
		mariobody.respawn();

		switch(sp.getSpawnType()) {
			case PIPEWARP:
				mariobody.setPosAndVel(sp.calcBeginOffsetFromSpawn(mariobody.getB2BodySize()), new Vector2(0f, 0f));
				exitingSpawnpoint = sp;
				marioSpriteOffset.set(getSpawnExitSpriteBeginOffset());
				break;
			case IMMEDIATE:
			default:
				mariobody.setPosAndVel(sp.getPosition(), new Vector2(0f, 0f));
				marioSpriteOffset.set(0f, 0f);
				exitingSpawnpoint = null;
				break;
		}
	}

	public void die() {
		marioIsDead = true;
	}

	public void giveCoin() {
		agency.playSound(AudioInfo.SOUND_COIN);
		givePoints(PointAmount.P200, false);
		coinTotal++;
	}

	public PointAmount givePoints(PointAmount amt, boolean relative) {
		PointAmount actualAmt = amt;
		if(relative) {
			// relative points do not stack when mario is onground
			if(!mariobody.isOnGround()) {
				if(consecBouncePoints.increment().getIntAmt() >= amt.getIntAmt()) {
					consecBouncePoints = consecBouncePoints.increment();
					actualAmt = consecBouncePoints;
				}
				else
					consecBouncePoints = amt;
			}
		}

		if(actualAmt == PointAmount.P1UP)
			give1UP();
		else
			pointTotal += actualAmt.getIntAmt();

		return actualAmt;
	}

	public void give1UP() {
		extraLives++;
	}

	// return null unless mario needs to warp
	public GuideSpawner getWarpSpawnpoint() {
		if(mariobody.getPipeToEnter() != null &&
				(curState == MarioState.PIPE_ENTRYH || curState == MarioState.PIPE_ENTRYV) &&
				stateTimer > PIPE_WARPENTRYTIME) {
			return mariobody.getPipeToEnter().getWarpExit();
		}
		return null;
	}

	private Vector2 getPipeEntrySpriteEndOffset(PipeWarp pipeToEnter) {
		switch(pipeToEnter.getDirection()) {
			case RIGHT:
				return new Vector2(PIPE_WARPWIDTH, 0f);
			case UP:
				return new Vector2(0f, PIPE_WARPHEIGHT);
			case LEFT:
				return new Vector2(-PIPE_WARPWIDTH, 0f);
			case DOWN:
			default:
				return new Vector2(0f, -PIPE_WARPHEIGHT);
		}
	}

	private Vector2 getSpawnExitSpriteBeginOffset() {
		switch(exitingSpawnpoint.getDirection()) {
			case RIGHT:
				return new Vector2(-PIPE_WARPWIDTH, 0f);
			case UP:
				return new Vector2(0f, -PIPE_WARPHEIGHT);
			case LEFT:
				return new Vector2(PIPE_WARPWIDTH, 0f);
			case DOWN:
			default:
				return new Vector2(0f, PIPE_WARPHEIGHT);
		}
	}

	public float getStateTimer() {
		return stateTimer;
	}

	public Room getCurrentRoom() {
		return mariobody.getCurrentRoom();
	}

	public float getLevelTimeRemaining() {
		return levelTimeRemaining;
	}

	public int getCoinTotal() {
		return coinTotal;
	}

	public int getPointTotal() {
		return pointTotal;
	}

	public int getExtraLives() {
		return extraLives;
	}

	/*
	 * Return true if marioIsDead flag is set and the update method has been called at least once while marioIsDead.
	 * Return false otherwise.
	 * This is done so the getStateTimer method returns the correct state time when mario is dead. 
	 */
	public boolean isDead() {
		if(marioIsDead && curState == MarioState.DEAD)
			return true;
		return false;
	}

	public boolean isBig() {
		return (curPowerState != MarioPowerState.SMALL);
	}

	public boolean isAtLevelEnd() {
		return mariobody.getLevelEndContacted() != null;
	}

	public boolean isPowerStarOn() {
		return powerStarTimer > 0f;
	}

	public boolean isDmgInvincibleOn() {
		return dmgInvincibleTime > 0f;
	}

	public boolean isOnGround() {
		return mariobody.isOnGround();
	}

	@Override
	public Vector2 getPosition() {
		return mariobody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return mariobody.getBounds();
	}

	@Override
	public void dispose() {
		mariobody.dispose();
	}
}
