package kidridicarus.game.SMB.agent.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.agentscript.ScriptedSpriteState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.MoveAdvice;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentObserverPlus;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.game.SMB.agent.other.Flagpole;
import kidridicarus.game.SMB.agent.other.FloatingPoints;
import kidridicarus.game.SMB.agent.other.LevelEndTrigger;
import kidridicarus.game.SMB.agentbody.player.MarioBody;
import kidridicarus.game.SMB.agentbody.player.MarioBody.MarioBodyState;
import kidridicarus.game.SMB.agentsprite.player.MarioSprite;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo.PointAmount;

/*
 * TODO:
 * -the body physics code has only been tested with non-moving surfaces, needs to be tested with moving platforms
 * -mario will sometimes not go down a pipe warp even though he is in the right place on top of the pipe - fix this
 */
public class Mario extends Agent implements UpdatableAgent, DrawableAgent, PlayerAgent, PowerupTakeAgent {
	public enum MarioAgentState { PLAY, FIREBALL, DEAD }

	private static final float LEVEL_MAX_TIME = 300f;

	private static final float DMG_INVINCIBLE_TIME = 3f;
	private static final float FIREBALL_OFFSET = UInfo.P2M(8f);
	private static final float TIME_PER_FIREBALL = 0.5f;
	private static final float POWERSTAR_TIME = 15f;

	public enum MarioPowerState { SMALL, BIG, FIRE }

	private MarioSupervisor supervisor;
	private MarioObserver observer;
	private MarioBody mBody;
	private MarioSprite marioSprite;
	private MarioPowerState curPowerState;
	private MarioAgentState curAgentState;
	private float stateTimer;

	private boolean marioIsDead;
	private boolean prevFrameAdvisedShoot;
	private boolean isDmgInvincible;
	private float dmgInvincibleTime;
	private float fireballTimer;
	private float powerStarTimer;
	private float levelTimeRemaining;
	private boolean isLevelEndContacted;
	private int extraLives;
	private int coinTotal;
	private int pointTotal;
	private PointAmount consecBouncePoints;
	// powerup received
	private PowType powerupRec;

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
		isLevelEndContacted = false;

		curAgentState = MarioAgentState.PLAY;
		stateTimer = 0f;

		extraLives = 2;
		coinTotal = pointTotal = 0;
		consecBouncePoints = PointAmount.ZERO;

		mBody = new MarioBody(this, agency, Agent.getStartPoint(properties), true, false);
		marioSprite = new MarioSprite(agency.getAtlas(), mBody.getPosition(), curPowerState);

		supervisor = new MarioSupervisor(this);
		observer = new MarioObserver(this, agency.getAtlas());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_TOP);
	}

	@Override
	public void update(float delta) {
		MarioBodyState bodyState;
		MarioAgentState nextAgentState;
		boolean isStarPowered;
		MoveAdvice advice = supervisor.pollMoveAdvice(); 

		levelTimeRemaining -= delta;

		if(supervisor.isRunningScript()) {
			// if a script is running with move advice, then switch advice to the scripted move advice
			if(supervisor.isRunningScriptMoveAdvice())
				advice = supervisor.getScriptAgentState().scriptedMoveAdvice;
			else {
				// use the scripted agent state
				mBody.useScriptedBodyState(supervisor.getScriptAgentState().scriptedBodyState);
				MarioBodyState scriptedBodyState;
				ScriptedSpriteState sss = supervisor.getScriptAgentState().scriptedSpriteState;
				switch(sss.spriteState) {
					case CLIMB:
						scriptedBodyState = MarioBodyState.CLIMB;
						break;
					case MOVE:
						scriptedBodyState = MarioBodyState.WALKRUN;
						break;
					case STAND:
					default:
						scriptedBodyState = MarioBodyState.STAND;
						break;
				}
				marioSprite.update(delta, sss.position, MarioAgentState.PLAY, scriptedBodyState, curPowerState,
						sss.facingRight, isDmgInvincible, false, mBody.isBigBody(), sss.moveDir);

				// TODO only run this next line when script ends,
				//   to prevent mario getting pipe warp contact where there is none
				mBody.resetPipeToEnter();

				return;
			}
		}

		// reset consecutive bounce points
		if(mBody.isOnGround())
			consecBouncePoints = PointAmount.ZERO;

		bodyState = MarioBodyState.STAND;
		nextAgentState = processMarioAgentState(delta, advice);
		// mario special states (e.g. end level script) override regular body states (e.g. mario use powerup) 
		if(nextAgentState == MarioAgentState.PLAY || nextAgentState == MarioAgentState.FIREBALL) {
			processDamage(delta);
			processPowerups();
			bodyState = mBody.update(delta, advice, curPowerState);
		}

		stateTimer = nextAgentState == curAgentState ? stateTimer+delta : 0f;
		curAgentState = nextAgentState;

		isStarPowered = false;
		if(powerStarTimer > 0f) {
			isStarPowered = true;
			powerStarTimer -= delta;
		}

		marioSprite.update(delta, mBody.getPosition(), curAgentState, bodyState,
				curPowerState, mBody.isFacingRight(), isDmgInvincible, isStarPowered, mBody.isBigBody(), null);

		prevFrameAdvisedShoot = advice.action0;
	}

	// Process the body and return a character state based on the findings.
	private MarioAgentState processMarioAgentState(float delta, MoveAdvice advice) {
		if(marioIsDead) {
			if(curAgentState != MarioAgentState.DEAD) {
				observer.stopAllMusic();
				agency.playSound(AudioInfo.Sound.SMB.MARIO_DIE);

				mBody.disableAllContacts();

				mBody.setVelocity(0f, 0f);
				mBody.applyImpulse(new Vector2(0, 4f));
			}
			// make sure mario doesn't move left or right while dead
			mBody.zeroVelocity(true, false);
			return MarioAgentState.DEAD;
		}
		else if(isUseLevelEndTrigger()) {
			isLevelEndContacted = true;
			return curAgentState;
		}
		// flagpole contact and use?
		else if(isUseFlagpole())
			return curAgentState;
		// scripted pipe entrance
		else if(mBody.getPipeToEnter() != null) {
			mBody.getPipeToEnter().use(this);
			return curAgentState;
		}
		// otherwise the player has control, because no script is runnning
		else {
			if(processFireball(delta, advice.action0))
				return MarioAgentState.FIREBALL;
			else
				return MarioAgentState.PLAY;
		}
	}

	private boolean isUseLevelEndTrigger() {
		// check for end level trigger contact, and use it if found
		LevelEndTrigger leTrigger = mBody.getFirstContactByClass(LevelEndTrigger.class);
		if(leTrigger != null) {
			leTrigger.use(this);
			return true;
		}
		return false;
	}

	private boolean isUseFlagpole() {
		// check for end level flagpole contact, and use it if found
		Flagpole flagpole = mBody.getFirstContactByClass(Flagpole.class);
		if(flagpole != null) {
			flagpole.use(this);
			return true;
		}
		return false;
	}

	// mario can shoot fireballs two at a time, but must wait if his "fireball timer" runs low
	private boolean processFireball(float delta, boolean shoot) {
		if(curAgentState != MarioAgentState.PLAY)
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
		if(marioIsDead && curAgentState == MarioAgentState.DEAD)
			return true;
		return false;
	}

	public boolean isBig() {
		return (curPowerState != MarioPowerState.SMALL);
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
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public AgentObserverPlus getObserver() {
		return observer;
	}

	@Override
	public void applyPowerup(PowType pt) {
		powerupRec = pt;
	}

	// unchecked cast to T warnings ignored because T is checked with class.equals(cls) 
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key, Object defaultValue, Class<T> cls) {
		if(key.equals(CommonKV.Script.KEY_FACINGRIGHT) && Boolean.class.equals(cls)) {
			Boolean he = mBody.isFacingRight();
			return (T) he;
		}
		else if(key.equals(CommonKV.Script.KEY_SPRITESTATE) && SpriteState.class.equals(cls)) {
			SpriteState he = SpriteState.STAND;
			return (T) he;
		}
		else if(key.equals(CommonKV.Script.KEY_SPRITESIZE) && Vector2.class.equals(cls)) {
			Vector2 he = new Vector2(marioSprite.getWidth(), marioSprite.getHeight());
			return (T) he;
		}
		return properties.get(key, defaultValue, cls);
	}

	@Override
	public boolean isAtLevelEnd() {
		return isLevelEndContacted;
	}

	@Override
	public void dispose() {
		mBody.dispose();
	}
}
