package kidridicarus.roles.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.InfoSMB.PointAmount;
import kidridicarus.InfoSMB.PowerupType;
import kidridicarus.bodies.SMB.MarioBody;
import kidridicarus.bodies.SMB.MarioBody.MarioBodyState;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.robot.SMB.MarioFireball;
import kidridicarus.roles.robot.SMB.PipeWarp;
import kidridicarus.sprites.SMB.MarioSprite;
import kidridicarus.tools.BasicInputs;
import kidridicarus.worldrunner.Spawnpoint;
import kidridicarus.worldrunner.WorldRunner;

/*
 * TODO:
 * -the body physics code has only been tested with non-moving surfaces, needs to be tested with moving platforms
 * -if wants to go down pipe then do not process ducking code - check this
 * -when big mario with powerstar dies, the dead sprite is stretched vertically and keeps the power star
 *  animation - remove the animation and unstretch mario
 * -mario will sometimes not go down a pipe warp even though he is in the right place on top of the pipe, fix this
 */
public class MarioRole implements PlayerRole {
	public enum MarioRoleState { PLAY, FIREBALL, DEAD, END1_SLIDE, END2_WAIT1, END3_WAIT2, END4_FALL, END5_BRAKE,
		END6_RUN, END99, PIPE_ENTRYH, PIPE_EXITH, PIPE_ENTRYV, PIPE_EXITV };

	private static final float DMG_INVINCIBLE_TIME = 3f;
	private static final float FIREBALL_OFFSET = GameInfo.P2M(8f);
	private static final float TIME_PER_FIREBALL = 0.5f;
	private static final float POWERSTAR_TIME = 15f;
	private static final float FLAG_SLIDE_VELOCITY = -0.9f;
	private static final float END_FLAGWAIT = 0.4f;
	private static final float END_BRAKETIME = 0.02f;
	private static final Vector2 FLAG_JUMPOFF_VEL = new Vector2(1.0f, 1.0f);

	private static final float PIPE_WARPHEIGHT = GameInfo.P2M(32);
	private static final float PIPE_WARPWIDTH = GameInfo.P2M(16);
	private static final float PIPE_WARPENTRYTIME = 0.7f;

	public enum MarioPowerState { SMALL, BIG, FIRE };

	private WorldRunner runner;
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
	private Spawnpoint exitingSpawnpoint;

	private MarioRoleState curState;
	private float stateTimer;

	private int numLives;
	private int coinTotal;
	private int pointTotal;
	private PointAmount flyingPoints;

	public MarioRole(WorldRunner runner, Vector2 position) {
		this.runner = runner;

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

		curState = MarioRoleState.PLAY;
		stateTimer = 0f;

		numLives = 2;
		coinTotal = pointTotal = 0;
		flyingPoints = PointAmount.ZERO;

		// physic
		mariobody = new MarioBody(this, runner, position, true, false);

		// graphic
		marioSprite = new MarioSprite(runner.getAtlas(), position, MarioBodyState.STAND, curPowerState,
				mariobody.isFacingRight());
	}

	@Override
	public void update(float delta, BasicInputs bi) {
		MarioBodyState bodyState;
		MarioRoleState nextState;
		boolean isStarPowered;

		bodyState = MarioBodyState.STAND;
		nextState = processRoleState(delta, bi);
		// role states (e.g. end level script) override regular body states (e.g. mario use powerup) 
		if(nextState == MarioRoleState.PLAY || nextState == MarioRoleState.FIREBALL) {
			processDamage(delta);
			processPowerups();
			bodyState = mariobody.update(delta, bi, curPowerState);
		}
		else if(nextState == MarioRoleState.END99) {
			// at this point the player has hit the end of level trigger, so raise the castle flag
			if(nextState != curState)
				runner.triggerCastleFlag();
		}

		stateTimer = nextState == curState ? stateTimer + delta : 0f;
		curState = nextState;

		isStarPowered = false;
		if(powerStarTimer > 0f) {
			isStarPowered = true;
			powerStarTimer -= delta;
			// restart regular music when powerstar powerup finishes
			if(powerStarTimer <= 0f)
				runner.startRoomMusic();
		}

		marioSprite.update(delta, mariobody.getPosition().cpy().add(marioSpriteOffset), curState, bodyState,
				curPowerState, mariobody.isFacingRight(), isDmgInvincible, isStarPowered, mariobody.isBigBody());

		wantsToRunOnPrevUpdate = bi.wantsToRun;
	}

	// Process the body and return a character state based on the findings.
	private MarioRoleState processRoleState(float delta, BasicInputs bi) {
		// scripted level end sequence
		if(mariobody.getLevelEndTouched() != null) {
			mariobody.zeroVelocity(true, true);
			return MarioRoleState.END99;
		}
		// scripted dead sequence
		else if(marioIsDead) {
			if(curState != MarioRoleState.DEAD) {
				runner.stopRoomMusic();
				runner.playSound(GameInfo.SOUND_MARIODIE);

				mariobody.disableContacts();

				mariobody.setVelocity(0f, 0f);
				mariobody.applyImpulse(new Vector2(0, 4f));
			}
			// make sure mario doesn't move left or right while dead
			mariobody.zeroVelocity(true, false);
			return MarioRoleState.DEAD;
		}
		// scripted flagpole level end sequence
		else if(mariobody.getFlagpoleTouched() != null) {
			// scripted level end sequence using curCharState and stateTimer
			switch(curState) {
				case END1_SLIDE:
					// switch sides if necessary when hit ground
					if(mariobody.isOnGround())
						return MarioRoleState.END2_WAIT1;
					// sliding down
					else {
						mariobody.setVelocity(0f, FLAG_SLIDE_VELOCITY);
						return MarioRoleState.END1_SLIDE;
					}
				case END2_WAIT1:
					if(mariobody.getFlagpoleTouched().isAtBottom()) {
						mariobody.setFacingRight(false);
						// if mario is on left side of flagpole, move him to right side
						if(mariobody.getFlagpoleTouched().getPosition().x > mariobody.getPosition().x) {
							mariobody.setPosAndVel(mariobody.getPosition().cpy().add(2f *
									(mariobody.getFlagpoleTouched().getPosition().x -
									mariobody.getPosition().x), 0f),
									new Vector2(0f, 0f));
						}
						return MarioRoleState.END3_WAIT2;
					}
					else
						return MarioRoleState.END2_WAIT1;
				case END3_WAIT2:
					if(stateTimer > END_FLAGWAIT) {
						// switch to first walk frame and push mario to right
						mariobody.enableGravity();
						mariobody.applyImpulse(FLAG_JUMPOFF_VEL);
						return MarioRoleState.END4_FALL;
					}
					else
						return MarioRoleState.END3_WAIT2;
				case END4_FALL:
					if(mariobody.isOnGround())
						return MarioRoleState.END5_BRAKE;
					else
						return MarioRoleState.END4_FALL;
				case END5_BRAKE:
					if(stateTimer > END_BRAKETIME) {
						mariobody.setFacingRight(true);
						runner.startSinglePlayMusic(GameInfo.MUSIC_LEVELEND);
						mariobody.resetFlagpoleTouched();
						return MarioRoleState.END6_RUN;
					}
					else
						return MarioRoleState.END5_BRAKE;
				case END6_RUN:
 					mariobody.moveBodyLeftRight(true, false);
					return MarioRoleState.END6_RUN;
				// first level end state
				default:
					mariobody.getFlagpoleTouched().startDrop();
					mariobody.disableGravity();
					mariobody.zeroVelocity(true, true);

					runner.stopRoomMusic();
					runner.playSound(GameInfo.SOUND_FLAGPOLE);

					return MarioRoleState.END1_SLIDE;
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
					runner.playSound(GameInfo.SOUND_POWERDOWN);

					mariobody.disableContacts();

					mariobody.disableGravity();
					mariobody.zeroVelocity(true, true);
					if(mariobody.getPipeToEnter().getDirection().isHorizontal())
						return MarioRoleState.PIPE_ENTRYH;
					else
						return MarioRoleState.PIPE_ENTRYV;
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
						return MarioRoleState.PLAY;
					}
					else {
						marioSpriteOffset.scl((PIPE_WARPENTRYTIME - stateTimer) / PIPE_WARPENTRYTIME);
						return curState;
					}
				// first frame of pipe exit
				default:
					if(exitingSpawnpoint.getDirection().isHorizontal())
						return MarioRoleState.PIPE_EXITH;
					else
						return MarioRoleState.PIPE_EXITV;
			}
		}
		// otherwise the player has control, because no script is runnning
		else {
			if(processFireball(delta, bi))
				return MarioRoleState.FIREBALL;
			else
				return MarioRoleState.PLAY;
		}
	}

	// mario can shoot fireballs two at a time, but must wait if his "fireball timer" runs low
	private boolean processFireball(float delta, BasicInputs bi) {
		if(curState != MarioRoleState.PLAY)
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
		MarioFireball ball;

		if(mariobody.isFacingRight())
			ball = new MarioFireball(this, runner, mariobody.getPosition().cpy().add(FIREBALL_OFFSET, 0f), true);
		else
			ball = new MarioFireball(this, runner, mariobody.getPosition().cpy().add(-FIREBALL_OFFSET, 0f), false);

		runner.addRobot(ball);
		runner.playSound(GameInfo.SOUND_FIREBALL);
	}

	private void processPowerups() {
		// apply powerup if received
		switch(receivedPowerup) {
			case MUSH1UP:
				runner.givePlayerPoints(this, PointAmount.UP1, true, mariobody.getPosition(), GameInfo.P2M(16),
						false);
				break;
			case MUSHROOM:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mariobody.setBodyPosVelAndSize(mariobody.getPosition().add(0f, GameInfo.P2M(8f)),
							mariobody.getVelocity(), true);
					runner.playSound(GameInfo.SOUND_POWERUP_USE);
				}
				runner.givePlayerPoints(this, PointAmount.P1000, true, mariobody.getPosition(), GameInfo.P2M(16),
						false);
				break;
			case FIREFLOWER:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mariobody.setBodyPosVelAndSize(mariobody.getPosition().add(0f, GameInfo.P2M(8f)),
							mariobody.getVelocity(), true);
					runner.playSound(GameInfo.SOUND_POWERUP_USE);
				}
				else if(curPowerState == MarioPowerState.BIG) {
					curPowerState = MarioPowerState.FIRE;
					runner.playSound(GameInfo.SOUND_POWERUP_USE);
				}
				runner.givePlayerPoints(this, PointAmount.P1000, true, mariobody.getPosition(), GameInfo.P2M(16),
						false);
				break;
			case POWERSTAR:
				powerStarTimer = POWERSTAR_TIME;
				runner.stopRoomMusic();
				runner.playSound(GameInfo.SOUND_POWERUP_USE);
				runner.startSinglePlayMusic(GameInfo.MUSIC_STARPOWER);
				runner.givePlayerPoints(this, PointAmount.P1000, true, mariobody.getPosition(), GameInfo.P2M(16),
						false);
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
					mariobody.setBodyPosVelAndSize(mariobody.getPosition().sub(0f, GameInfo.P2M(8f)),
							mariobody.getVelocity(), false);
				}
				
				startDmgInvincibility();
				runner.playSound(GameInfo.SOUND_POWERDOWN);
			}
			// die if small and not invincible
			else
				marioIsDead = true;
		}
	}

	private void startDmgInvincibility() {
		isDmgInvincible = true;
		dmgInvincibleTime = DMG_INVINCIBLE_TIME;

		mariobody.disableRobotContact();
	}

	private void endDmgInvincibility() {
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;

		mariobody.enableRobotContact();
	}

	public void applyPowerup(PowerupType powerup) {
		// TODO: check if already received powerup, and check for rank
		receivedPowerup = powerup;
	}

	/*
	 * Return true if marioIsDead flag is set and the update method has been called at least once while marioIsDead.
	 * Return false otherwise.
	 * This is done so the getStateTimer method returns the correct state time when mario is dead. 
	 */
	@Override
	public boolean isDead() {
		if(marioIsDead && curState == MarioRoleState.DEAD)
			return true;
		return false;
	}

	@Override
	public void draw(Batch batch) {
		if(mariobody.getLevelEndTouched() == null)
			marioSprite.draw(batch);
	}

	public boolean isBig() {
		return (curPowerState != MarioPowerState.SMALL);
	}

	@Override
	public boolean isAtLevelEnd() {
		return mariobody.getLevelEndTouched() != null;
	}

	// return null unless mario needs to warp
	@Override
	public Spawnpoint getWarpSpawnpoint() {
		if(mariobody.getPipeToEnter() != null &&
				(curState == MarioRoleState.PIPE_ENTRYH || curState == MarioRoleState.PIPE_ENTRYV) &&
				stateTimer > PIPE_WARPENTRYTIME) {
			return mariobody.getPipeToEnter().getWarpExit();
		}
		return null;
	}

	@Override
	public SpriteDrawOrder getDrawOrder() {
		if(mariobody.getPipeToEnter() != null || exitingSpawnpoint != null)
			return SpriteDrawOrder.MIDDLE;
		return SpriteDrawOrder.TOP;
	}

	@Override
	public void respawn(Spawnpoint sp) {
		mariobody.respawn();

		switch(sp.getSpawnType()) {
			case PIPEWARP:
				mariobody.setPosAndVel(sp.calcBeginOffsetFromSpawn(mariobody.getB2BodySize()), new Vector2(0f, 0f));
				exitingSpawnpoint = sp;
				marioSpriteOffset.set(getSpawnExitSpriteBeginOffset());
				break;
			case IMMEDIATE:
			default:
				mariobody.setPosAndVel(sp.getCenter(), new Vector2(0f, 0f));
				marioSpriteOffset.set(0f, 0f);
				exitingSpawnpoint = null;
				break;
		}
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

	public boolean isPowerStarOn() {
		return powerStarTimer > 0f;
	}

	public boolean isDmgInvincibleOn() {
		return dmgInvincibleTime > 0f;
	}

	@Override
	public Vector2 getPosition() {
		return mariobody.getPosition();
	}

	@Override
	public float getStateTimer() {
		return stateTimer;
	}

	public void giveCoin() {
		runner.playSound(GameInfo.SOUND_COIN);
		runner.givePlayerPoints(this, PointAmount.P200, false, null, 0, false);
		coinTotal++;
	}

	public void givePoints(PointAmount amt) {
		pointTotal += amt.getAmt();
	}

	public int getCoinTotal() {
		return coinTotal;
	}

	public int getPointTotal() {
		return pointTotal;
	}

	public void die() {
		marioIsDead = true;
	}

	public void incrementFlyingPoints() {
		flyingPoints = flyingPoints.increment();
	}

	public void resetFlyingPoints() {
		flyingPoints = PointAmount.ZERO;
	}

	public PointAmount getFlyingPoints() {
		return flyingPoints;
	}

	public void setFlyingPoints(PointAmount amount) {
		flyingPoints = amount;
	}

	@Override
	public boolean isOnGround() {
		return mariobody.isOnGround();
	}

	public void give1UP() {
		numLives++;
	}

	public int getNumLives() {
		return numLives;
	}

	@Override
	public void dispose() {
		mariobody.dispose();
	}
}
