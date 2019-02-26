package kidridicarus.game.agent.SMB.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.agent.AgentObserver;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.agent.general.GuideSpawner;
import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.optional.PlayerAgent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.SMB.FloatingPoints;
import kidridicarus.game.agent.SMB.WarpPipe;
import kidridicarus.game.agent.body.SMB.player.MarioBody;
import kidridicarus.game.agent.body.SMB.player.MarioBody.MarioBodyState;
import kidridicarus.game.agent.sprite.SMB.player.MarioSprite;
import kidridicarus.game.guide.GameAdvice;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo.PointAmount;

/*
 * TODO:
 * -the body physics code has only been tested with non-moving surfaces, needs to be tested with moving platforms
 * -mario will sometimes not go down a pipe warp even though he is in the right place on top of the pipe - fix this
 */
public class Mario extends Agent implements /*AdvisableAgent,*/ PlayerAgent {
	public enum MarioState { PLAY, FIREBALL, DEAD, END1_SLIDE, END2_WAIT1, END3_WAIT2, END4_FALL, END5_BRAKE,
		END6_RUN, END99, PIPE_ENTRYH, PIPE_EXITH, PIPE_ENTRYV, PIPE_EXITV }

	private static final float LEVEL_MAX_TIME = 300f;

	private static final float DMG_INVINCIBLE_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float TIME_PER_FIREBALL = 0.5f;
	private static final float POWERSTAR_TIME = 15f;
	private static final float FLAG_SLIDE_VELOCITY = -0.9f;
	private static final float END_FLAGWAIT = 0.4f;
	private static final float END_BRAKETIME = 0.02f;
	private static final Vector2 FLAG_JUMPOFF_VEL = new Vector2(1.0f, 1.0f);

	private static final float PIPE_WARPHEIGHT = UInfo.P2M(32);
	private static final float PIPE_WARPWIDTH = UInfo.P2M(16);
	private static final float PIPE_WARPENTRYTIME = 0.7f;

	public enum MarioPowerState { SMALL, BIG, FIRE }

	private MarioSprite marioSprite;
	private MarioBody mBody;
	private MarioPowerState curPowerState;
	private MarioState curState;
	private float stateTimer;

	private Vector2 marioSpriteOffset;
	private GuideSpawner exitingSpawnpoint;
	private boolean marioIsDead;
	private boolean prevFrameAdvisedShoot;
	private boolean isDmgInvincible;
	private float dmgInvincibleTime;
	private float fireballTimer;
	private float powerStarTimer;
	private float levelTimeRemaining;
	private int extraLives;
	private int coinTotal;
	private int pointTotal;
	private PointAmount consecBouncePoints;
	// powerup received
	private PowType powerupRec;
	// non-character powerup received
	private PowType nonCharPowerupRec;

	private AgentObserver observer;
	private MarioSupervisor supervisor;

	public Mario(Agency agency, AgentDef adef) {
		super(agency, adef);

		marioIsDead = false;
		prevFrameAdvisedShoot = false;
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		fireballTimer = TIME_PER_FIREBALL * 2f;
		powerStarTimer = 0f;
		marioSpriteOffset = new Vector2(0f, 0f);

		curPowerState = MarioPowerState.SMALL;

		powerupRec = PowType.NONE;
		nonCharPowerupRec = PowType.NONE;

		exitingSpawnpoint = null;

		levelTimeRemaining = LEVEL_MAX_TIME;

		curState = MarioState.PLAY;
		stateTimer = 0f;

		extraLives = 2;
		coinTotal = pointTotal = 0;
		consecBouncePoints = PointAmount.ZERO;

		// physic
		mBody = new MarioBody(this, agency, adef.bounds.getCenter(new Vector2()), true, false);

		// graphic
		marioSprite = new MarioSprite(agency.getAtlas(), adef.bounds.getCenter(new Vector2()),
				curPowerState);

		observer = new AgentObserver(this);
		supervisor = new MarioSupervisor(this);

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_TOP);
	}

	@Override
	public void update(float delta) {
		MarioBodyState bodyState;
		MarioState nextState;
		boolean isStarPowered;
		GameAdvice advice = supervisor.pollFrameAdvice(); 

		levelTimeRemaining -= delta;

		// check for warp movement and respawn if necessary
		processRespawn();

		// reset consecutive bounce points
		if(mBody.isOnGround())
			consecBouncePoints = PointAmount.ZERO;

		bodyState = MarioBodyState.STAND;
		nextState = processMarioState(delta, advice);
		// mario special states (e.g. end level script) override regular body states (e.g. mario use powerup) 
		if(nextState == MarioState.PLAY || nextState == MarioState.FIREBALL) {
			processDamage(delta);
			processPowerups();
			bodyState = mBody.update(delta, advice, curPowerState);
		}

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;

		isStarPowered = false;
		if(powerStarTimer > 0f) {
			isStarPowered = true;
			powerStarTimer -= delta;
		}

		marioSprite.update(delta, mBody.getPosition().cpy().add(marioSpriteOffset), curState, bodyState,
				curPowerState, mBody.isFacingRight(), isDmgInvincible, isStarPowered, mBody.isBigBody());

		prevFrameAdvisedShoot = advice.runShoot;
	}

	// Process the body and return a character state based on the findings.
	private MarioState processMarioState(float delta, GameAdvice advice) {
		// scripted level end sequence
		if(mBody.getLevelEndContacted() != null) {
			mBody.zeroVelocity(true, true);
			return MarioState.END99;
		}
		// scripted dead sequence
		else if(marioIsDead) {
			if(curState != MarioState.DEAD) {
				agency.stopMusic();
				agency.playSound(AudioInfo.Sound.SMB.MARIO_DIE);

				mBody.disableAllContacts();

				mBody.setVelocity(0f, 0f);
				mBody.applyImpulse(new Vector2(0, 4f));
			}
			// make sure mario doesn't move left or right while dead
			mBody.zeroVelocity(true, false);
			return MarioState.DEAD;
		}
		// scripted flagpole level end sequence
		else if(mBody.getFlagpoleContacted() != null) {
			// scripted level end sequence using curCharState and stateTimer
			switch(curState) {
				case END1_SLIDE:
					// switch sides if necessary when hit ground
					if(mBody.isOnGround())
						return MarioState.END2_WAIT1;
					// sliding down
					else {
						mBody.setVelocity(0f, FLAG_SLIDE_VELOCITY);
						return MarioState.END1_SLIDE;
					}
				case END2_WAIT1:
					if(mBody.getFlagpoleContacted().isAtBottom()) {
						mBody.setFacingRight(false);
						// if mario is on left side of flagpole, move him to right side
						if(mBody.getFlagpoleContacted().getPosition().x > mBody.getPosition().x) {
							mBody.setPosAndVel(mBody.getPosition().cpy().add(2f *
									(mBody.getFlagpoleContacted().getPosition().x -
									mBody.getPosition().x), 0f),
									new Vector2(0f, 0f));
						}
						return MarioState.END3_WAIT2;
					}
					else
						return MarioState.END2_WAIT1;
				case END3_WAIT2:
					if(stateTimer > END_FLAGWAIT) {
						// switch to first walk frame and push mario to right
						mBody.enableGravity();
						mBody.applyImpulse(FLAG_JUMPOFF_VEL);
						return MarioState.END4_FALL;
					}
					else
						return MarioState.END3_WAIT2;
				case END4_FALL:
					if(mBody.isOnGround())
						return MarioState.END5_BRAKE;
					else
						return MarioState.END4_FALL;
				case END5_BRAKE:
					if(stateTimer > END_BRAKETIME) {
						mBody.setFacingRight(true);
						agency.startSinglePlayMusic(AudioInfo.Music.SMB.LEVELEND);
						mBody.resetFlagpoleContacted();
						return MarioState.END6_RUN;
					}
					else
						return MarioState.END5_BRAKE;
				case END6_RUN:
 					mBody.moveBodyLeftRight(true, false);
					return MarioState.END6_RUN;
				// first frame of level end state
				default:
					mBody.getFlagpoleContacted().startDrop();
					mBody.disableGravity();
					mBody.zeroVelocity(true, true);

					agency.stopMusic();
					agency.playSound(AudioInfo.Sound.SMB.FLAGPOLE);

					return MarioState.END1_SLIDE;
			}
		}
		// scripted pipe entrance
		else if(mBody.getPipeToEnter() != null) {
			switch(curState) {
				// continuing pipe entry
				case PIPE_ENTRYH:
				case PIPE_ENTRYV:
					marioSpriteOffset.set(getPipeEntrySpriteEndOffset(mBody.getPipeToEnter()));
					if(stateTimer < PIPE_WARPENTRYTIME)
						marioSpriteOffset.scl(stateTimer / PIPE_WARPENTRYTIME);
					return curState;
				// first frame of pipe entry
				default:
					agency.playSound(AudioInfo.Sound.SMB.POWERDOWN);

					// Mario disappears behind the pipe as he moves into it
					agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_BOTTOM);

					mBody.disableAllContacts();

					mBody.disableGravity();
					mBody.zeroVelocity(true, true);
					if(mBody.getPipeToEnter().getDirection().isHorizontal())
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
						// Mario reappears in front of the pipe as he moves out of it
						agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_TOP);

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
			if(processFireball(delta, advice.runShoot))
				return MarioState.FIREBALL;
			else
				return MarioState.PLAY;
		}
	}

	// mario can shoot fireballs two at a time, but must wait if his "fireball timer" runs low
	private boolean processFireball(float delta, boolean shoot) {
		if(curState != MarioState.PLAY)
			return false;

		fireballTimer += delta;
		if(fireballTimer > TIME_PER_FIREBALL)
			fireballTimer = TIME_PER_FIREBALL;

		// fire a ball?
		if(curPowerState == MarioPowerState.FIRE && shoot && !prevFrameAdvisedShoot && fireballTimer > 0f) {
			fireballTimer -= TIME_PER_FIREBALL;
			throwFireball();
			return true;
		}

		return false;
	}

	private void throwFireball() {
		Vector2 offset;
		if(mBody.isFacingRight())
			offset = mBody.getPosition().cpy().add(FIREBALL_OFFSET, 0f);
		else
			offset = mBody.getPosition().cpy().add(-FIREBALL_OFFSET, 0f);

		agency.createAgent(MarioFireball.makeMarioFireballDef(offset, mBody.isFacingRight(), this));
		agency.playSound(AudioInfo.Sound.SMB.FIREBALL);
	}

	private void processPowerups() {
		// apply powerup if received
		switch(powerupRec) {
			case MUSH1UP:
				agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P1UP, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case MUSHROOM:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mBody.setBodyPosVelAndSize(mBody.getPosition().add(0f, UInfo.P2M(8f)),
							mBody.getVelocity(), true);
					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case FIREFLOWER:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mBody.setBodyPosVelAndSize(mBody.getPosition().add(0f, UInfo.P2M(8f)),
							mBody.getVelocity(), true);
					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				else if(curPowerState == MarioPowerState.BIG) {
					curPowerState = MarioPowerState.FIRE;
					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case POWERSTAR:
				powerStarTimer = POWERSTAR_TIME;
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				agency.startSinglePlayMusic(AudioInfo.Music.SMB.STARPOWER);
				agency.createAgent(FloatingPoints.makeFloatingPointsDef(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case NONE:
				break;
			// did mario receive a powerup that's not for his character?
			default:
				nonCharPowerupRec = powerupRec;
				break;
		}

		powerupRec = PowType.NONE;
	}

	private void processDamage(float delta) {
		if(dmgInvincibleTime > 0f)
			dmgInvincibleTime -= delta;
		else if(isDmgInvincible)
			endDmgInvincibility();

		// apply damage if received
		if(mBody.getAndResetTakeDamage()) {
			// fire mario becomes small mario
			// big mario becomes small mario
			if(curPowerState == MarioPowerState.FIRE || curPowerState == MarioPowerState.BIG) {
				curPowerState = MarioPowerState.SMALL;
				if(mBody.isDucking())
					mBody.setBodyPosVelAndSize(mBody.getPosition(), mBody.getVelocity(), false);
				else {
					mBody.setBodyPosVelAndSize(mBody.getPosition().sub(0f, UInfo.P2M(8f)),
							mBody.getVelocity(), false);
				}

				startDmgInvincibility();
				agency.playSound(AudioInfo.Sound.SMB.POWERDOWN);
			}
			// die if small and not invincible
			else
				marioIsDead = true;
		}
	}

	private void startDmgInvincibility() {
		isDmgInvincible = true;
		dmgInvincibleTime = DMG_INVINCIBLE_TIME;
	}

	private void endDmgInvincibility() {
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
	}


	@Override
	public void applyPowerup(PowType pt) {
		// TODO: check if already received powerup, and check for rank
		powerupRec = pt;
	}

	@Override
	public void draw(Batch batch) {
		if(mBody.getLevelEndContacted() == null)
			marioSprite.draw(batch);
	}

	public void die() {
		marioIsDead = true;
	}

	public void giveCoin() {
		agency.playSound(AudioInfo.Sound.SMB.COIN);
		givePoints(PointAmount.P200, false);
		coinTotal++;
	}

	public PointAmount givePoints(PointAmount amt, boolean relative) {
		PointAmount actualAmt = amt;
		// relative points do not stack when mario is onground
		if(relative && !mBody.isOnGround()) {
			if(consecBouncePoints.increment().getIntAmt() >= amt.getIntAmt()) {
				consecBouncePoints = consecBouncePoints.increment();
				actualAmt = consecBouncePoints;
			}
			else
				consecBouncePoints = amt;
		}

		if(actualAmt == PointAmount.P1UP)
			give1UP();
		else
			pointTotal += actualAmt.getIntAmt();

		return actualAmt;
	}

	private void give1UP() {
		extraLives++;
	}

	private void processRespawn() {
		// check for warp movement and respawn if necessary
		GuideSpawner sp = getWarpSpawnpoint(); 
		if(sp == null)
			return;

		mBody.resetPipeToEnter();

		switch(sp.getSpawnType()) {
			case PIPEWARP:
				mBody.setPosAndVel(sp.calcBeginOffsetFromSpawn(mBody.getBodySize()), new Vector2(0f, 0f));
				exitingSpawnpoint = sp;
				marioSpriteOffset.set(getSpawnExitSpriteBeginOffset());
				break;
			case IMMEDIATE:
			default:
				mBody.setPosAndVel(sp.getPosition(), new Vector2(0f, 0f));
				marioSpriteOffset.set(0f, 0f);
				exitingSpawnpoint = null;
				break;
		}
	}

	// return null unless mario needs to warp
	private GuideSpawner getWarpSpawnpoint() {
		if(mBody.getPipeToEnter() != null &&
				(curState == MarioState.PIPE_ENTRYH || curState == MarioState.PIPE_ENTRYV) &&
				stateTimer > PIPE_WARPENTRYTIME) {
			return mBody.getPipeToEnter().getWarpExit();
		}
		return null;
	}

	private Vector2 getPipeEntrySpriteEndOffset(WarpPipe pipeToEnter) {
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

	@Override
	public Room getCurrentRoom() {
		return mBody.getCurrentRoom();
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
		return mBody.getLevelEndContacted() != null;
	}

	public boolean isPowerStarOn() {
		return powerStarTimer > 0f;
	}

	public boolean isDmgInvincibleOn() {
		return dmgInvincibleTime > 0f;
	}

	public boolean isOnGround() {
		return mBody.isOnGround();
	}

	// get and reset the non character powerup received (if any)
	@Override
	public PowType pollNonCharPowerup() {
		PowType ret = nonCharPowerupRec;
		nonCharPowerupRec = PowType.NONE;
		return ret;
	}

	@Override
	public Vector2 getPosition() {
		return mBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return mBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return mBody.getVelocity();
	}

	@Override
	public AgentObserver getObserver() {
		return observer;
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public void dispose() {
		mBody.dispose();
	}
}
