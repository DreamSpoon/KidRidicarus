package kidridicarus.game.SMB.agent.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentObserverPlus;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.game.SMB.agent.other.FloatingPoints;
import kidridicarus.game.SMB.agentbody.player.MarioBody;
import kidridicarus.game.SMB.agentbody.player.MarioBody.MarioBodyState;
import kidridicarus.game.SMB.agentsprite.player.MarioSprite;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo.PointAmount;
import kidridicarus.game.play.GameAdvice;

/*
 * TODO:
 * -the body physics code has only been tested with non-moving surfaces, needs to be tested with moving platforms
 * -mario will sometimes not go down a pipe warp even though he is in the right place on top of the pipe - fix this
 */
public class Mario extends Agent implements UpdatableAgent, DrawableAgent, PlayerAgent, PowerupTakeAgent {
	public enum MarioState { PLAY, FIREBALL, DEAD, END1_SLIDE, END2_WAIT1, END3_WAIT2, END4_FALL, END5_BRAKE,
		END6_RUN, END99 }

	private static final float LEVEL_MAX_TIME = 300f;

	private static final float DMG_INVINCIBLE_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float TIME_PER_FIREBALL = 0.5f;
	private static final float POWERSTAR_TIME = 15f;
	private static final float FLAG_SLIDE_VELOCITY = -0.9f;
	private static final float END_FLAGWAIT = 0.4f;
	private static final float END_BRAKETIME = 0.02f;
	private static final Vector2 FLAG_JUMPOFF_VEL = new Vector2(1.0f, 1.0f);

	public enum MarioPowerState { SMALL, BIG, FIRE }

	private MarioSprite marioSprite;
	private MarioBody mBody;
	private MarioPowerState curPowerState;
	private MarioState curState;
	private float stateTimer;

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

	private MarioObserver observer;
	private MarioSupervisor supervisor;

	public Mario(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		marioIsDead = false;
		prevFrameAdvisedShoot = false;
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		fireballTimer = TIME_PER_FIREBALL * 2f;
		powerStarTimer = 0f;

		curPowerState = MarioPowerState.SMALL;

		powerupRec = PowType.NONE;

		levelTimeRemaining = LEVEL_MAX_TIME;

		curState = MarioState.PLAY;
		stateTimer = 0f;

		extraLives = 2;
		coinTotal = pointTotal = 0;
		consecBouncePoints = PointAmount.ZERO;

		mBody = new MarioBody(this, agency, Agent.getStartPoint(properties), true, false);
		marioSprite = new MarioSprite(agency.getAtlas(), mBody.getPosition(), curPowerState);

		observer = new MarioObserver(this, agency.getAtlas());
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

		if(supervisor.isScriptRunning()) {
			ScriptedAgentState def = supervisor.getScriptAgentState();
			mBody.useScriptedBodyState(def.scriptedBodyState);
			marioSprite.update(delta, def.scriptedSpriteState.position, curState, MarioBodyState.STAND, curPowerState,
					def.scriptedSpriteState.facingRight, isDmgInvincible, false, mBody.isBigBody());

			// TODO only run this next line when script ends,
			//   to prevent mario getting pipe warp contact where there is none
			mBody.resetPipeToEnter();

			return;
		}

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

		stateTimer = nextState == curState ? stateTimer+delta : 0f;
		curState = nextState;

		isStarPowered = false;
		if(powerStarTimer > 0f) {
			isStarPowered = true;
			powerStarTimer -= delta;
		}

		marioSprite.update(delta, mBody.getPosition(), curState, bodyState,
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
				observer.stopAllMusic();
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
						observer.startSinglePlayMusic(AudioInfo.Music.SMB.LEVELEND);

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

					observer.stopAllMusic();
					agency.playSound(AudioInfo.Sound.SMB.FLAGPOLE);

					return MarioState.END1_SLIDE;
			}
		}
		// scripted pipe entrance
		else if(mBody.getPipeToEnter() != null) {
			mBody.getPipeToEnter().use(this);
			return this.curState;
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

		agency.createAgent(MarioFireball.makeAP(offset, mBody.isFacingRight(), this));
		agency.playSound(AudioInfo.Sound.SMB.FIREBALL);
	}

	private void processPowerups() {
		// apply powerup if received
		switch(powerupRec) {
			case MUSH1UP:
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1UP, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case MUSHROOM:
				if(curPowerState == MarioPowerState.SMALL) {
					curPowerState = MarioPowerState.BIG;
					mBody.setBodyPosVelAndSize(mBody.getPosition().add(0f, UInfo.P2M(8f)),
							mBody.getVelocity(), true);
					agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				}
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1000, false,
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
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case POWERSTAR:
				powerStarTimer = POWERSTAR_TIME;
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_USE);
				observer.startSinglePlayMusic(AudioInfo.Music.SMB.STARPOWER);
				agency.createAgent(FloatingPoints.makeAP(PointAmount.P1000, false,
						mBody.getPosition(), UInfo.P2M(16), this));
				break;
			case NONE:
				break;
			// did mario receive a powerup that's not for his character?
			default:
				// tell supervisor!
				supervisor.applyNonMarioPowerup(powerupRec);
				break;
		}

		powerupRec = PowType.NONE;
	}

	private void processDamage(float delta) {
		// if invincible then remove incoming damage from the damage queue
		if(dmgInvincibleTime > 0f)
			dmgInvincibleTime -= delta;
		else if(isDmgInvincible)
			endDmgInvincibility();

		// check for damage, and reset the flag
		boolean dmg = mBody.getAndResetTakeDamage();
		// exit because no damage during power star time
		if(powerStarTimer > 0f)
			return;

		// exit if no damage received
		if(!dmg)
			return;

		// Apply damage if received:
		//   Fire mario becomes small mario,
		//   Big mario becomes small mario,
		//   Small mario becomes dead mario.
		if(curPowerState == MarioPowerState.FIRE || curPowerState == MarioPowerState.BIG) {
			curPowerState = MarioPowerState.SMALL;
			if(mBody.isDucking())
				mBody.setBodyPosVelAndSize(mBody.getPosition(), mBody.getVelocity(), false);
			else
				mBody.setBodyPosVelAndSize(mBody.getPosition().sub(0f, UInfo.P2M(8f)), mBody.getVelocity(), false);
			startDmgInvincibility();
			agency.playSound(AudioInfo.Sound.SMB.POWERDOWN);
		}
		else
			marioIsDead = true;
	}

	private void startDmgInvincibility() {
		isDmgInvincible = true;
		dmgInvincibleTime = DMG_INVINCIBLE_TIME;
	}

	private void endDmgInvincibility() {
		isDmgInvincible = false;
		dmgInvincibleTime = 0f;
		mBody.getAndResetTakeDamage();
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

	@Override
	public Vector2 getPosition() {
		return mBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return mBody.getBounds();
	}

	@Override
	public AgentObserverPlus getObserver() {
		return observer;
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public void applyPowerup(PowType pt) {
		powerupRec = pt;
	}

	// unchecked cast to T warnings ignored because T is checked with class.equals(poo) 
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key, Object defaultValue, Class<T> poo) {
		if(key.equals(GameKV.Script.KEY_FACINGRIGHT) && Boolean.class.equals(poo)) {
			Boolean he = mBody.isFacingRight();
			return (T) he;
		}
		else if(key.equals(GameKV.Script.KEY_SPRITESTATE) && SpriteState.class.equals(poo)) {
			SpriteState he = SpriteState.STAND;
			return (T) he;
		}
		else if(key.equals(GameKV.Script.KEY_SPRITESIZE) && Vector2.class.equals(poo)) {
			Vector2 he = new Vector2(marioSprite.getWidth(), marioSprite.getHeight());
			return (T) he;
		}
		return properties.get(key, defaultValue, poo);
	}

	@Override
	public void dispose() {
		mBody.dispose();
	}
}
