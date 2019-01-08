package kidridicarus.agent.Metroid.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.AdvisableAgent;
import kidridicarus.agent.Agent;
import kidridicarus.agent.PlayerAgent;
import kidridicarus.agent.body.Metroid.player.SamusBody;
import kidridicarus.agent.general.Room;
import kidridicarus.agent.optional.ContactDmgAgent;
import kidridicarus.agent.sprite.Metroid.player.SamusSprite;
import kidridicarus.guide.Advice;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.PowerupInfo.PowType;
import kidridicarus.tools.QQ;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.UInfo;

/*
 * TODO:
 * -samus loses JUMPSPIN when her y position goes below her jump start position
 */
public class Samus extends Agent implements AdvisableAgent, PlayerAgent {
	private static final float DAMAGE_INV_TIME = 0.8f;
	private static final Vector2 DAMAGE_KICK_SIDE_IMP = new Vector2(1.8f, 0f);
	private static final Vector2 DAMAGE_KICK_UP_IMP = new Vector2(0f, 1.3f);
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.P2MVector(8, 6);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.P2MVector(2, 20);
	private static final float SHOT_VEL = 2f;
	private static final float SHOOT_COOLDOWN = 0.15f;

	public enum ContactState { REGULAR, DAMAGE };
	public enum MoveState { STAND, RUN, JUMP, JUMPSPIN, BALL };

	private Advice advice;
	private SamusBody sBody;
	private SamusSprite sSprite;

	private MoveState curMoveState;
	private float moveStateTimer;
	private ContactState curContactState;
	private float contactStateTimer;

	private boolean isFacingRight;
	private boolean isFacingUp;
	private boolean isJumpForceEnabled;
	// the last jump must land before the next jump can start
	private boolean isLastJumpLandable;
	private boolean isNextJumpEnabled;
	private Vector2 damagePos = new Vector2();
	private boolean isDrawnLastFrame;
	private float shootResetTime;
	private float startJumpY;
	private boolean isJumpSpinAvailable;

	public Samus(Agency agency, AgentDef adef) {
		super(agency, adef);

		advice = new Advice();
		curMoveState = MoveState.STAND;
		moveStateTimer = 0f;
		curContactState = ContactState.REGULAR;
		contactStateTimer = 0f;
		isFacingRight = true;
		isFacingUp = false;
		isJumpForceEnabled = true;
		isLastJumpLandable = true;
		isNextJumpEnabled = true;
		isDrawnLastFrame = false;
		shootResetTime = agency.getGlobalTimer();
		startJumpY = 0;
		isJumpSpinAvailable = false;

		sBody = new SamusBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		sSprite = new SamusSprite(agency.getAtlas(), sBody.getPosition());

		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		processContactState(delta);
		processMoveState(delta);
		processSpriteState(delta);
		advice.clear();
	}

	private void processContactState(float delta) {
		ContactState nextContactState = ContactState.REGULAR;
		switch(curContactState) {
			case REGULAR:
				for(Agent a : sBody.getContactsByClass(ContactDmgAgent.class)) {
					ContactDmgAgent cda = (ContactDmgAgent) a;
					if(cda.isContactDamage()) {
						nextContactState = ContactState.DAMAGE;
						doContactDamage(a.getPosition());
					}
				}
				break;
			case DAMAGE:
				if(contactStateTimer > DAMAGE_INV_TIME)
					nextContactState = ContactState.REGULAR;
				else
					nextContactState = ContactState.DAMAGE;
				break;
		}

		contactStateTimer = nextContactState == curContactState ? contactStateTimer+delta : 0f;
		curContactState = nextContactState;
	}

	private void doContactDamage(Vector2 position) {
		damagePos.set(position);
		// zero the y velocity
		sBody.setVelocity(sBody.getVelocity().x, 0);
		// apply a kick impulse to the left or right depending on other agent's position
		if(sBody.getPosition().x < position.x)
			sBody.applyImpulse(DAMAGE_KICK_SIDE_IMP.cpy().scl(-1f));
		else
			sBody.applyImpulse(DAMAGE_KICK_SIDE_IMP);

		// apply kick up impulse if the player is above the other agent
		if(sBody.getPosition().y > position.y)
			sBody.applyImpulse(DAMAGE_KICK_UP_IMP);

		if(curMoveState != MoveState.JUMPSPIN)
			isJumpForceEnabled = false;

		agency.playSound(AudioInfo.Sound.Metroid.HURT);
	}

	private void processMoveState(float delta) {
		MoveState nextMoveState = null;
		boolean jumpStart = false;
		// XOR the moveleft and moveright, becuase can't move left and right at same time
		boolean isMoveHorizontal = advice.moveRight^advice.moveLeft;

		switch(curMoveState) {
			case BALL:
				if(advice.moveUp) {
					// if the tile directly above samus is solid then disallow switch to stand
					Vector2 bodyTilePos = UInfo.getM2PTileForPos(sBody.getPosition());
					if(agency.isMapTileSolid(bodyTilePos.cpy().add(0, 1)))
						nextMoveState = MoveState.BALL;
					else {
						isFacingUp = true;
						nextMoveState = MoveState.STAND;
						sBody.switchToStandForm();
					}
				}
				else {
					nextMoveState = MoveState.BALL;
					sBody.doBounceCheck();
				}
				break;
			case STAND:
			case RUN:
				if(!isNextJumpEnabled && !advice.jump)
					isNextJumpEnabled = true;

				// if body is falling, or is not rising, then enable the next jump
				if(sBody.getVelocity().y <= UInfo.VEL_EPSILON)
					isLastJumpLandable = true;

				isFacingUp = advice.moveUp;

				// switch to ball form?
				if(advice.moveDown && sBody.isOnGround()) {
					nextMoveState = MoveState.BALL;
					sBody.switchToBallForm();
				}
				// jump?
				else if(advice.jump && sBody.isOnGround() && isLastJumpLandable && isNextJumpEnabled) {
					isLastJumpLandable = false;
					isJumpForceEnabled = true;
					jumpStart = true;
					if(isMoveHorizontal)
						isJumpSpinAvailable = true;
					nextMoveState = MoveState.JUMP;
					startJumpY = sBody.getPosition().y;
					agency.playSound(AudioInfo.Sound.Metroid.JUMP);
				}
				else {
					// run = shoot
					if(advice.run && shootResetTime <= agency.getGlobalTimer()) {
						shootResetTime = agency.getGlobalTimer() + SHOOT_COOLDOWN;
						AgentDef adef;
						Vector2 position = new Vector2();
						Vector2 velocity = new Vector2();
						if(isFacingUp) {
							if(isFacingRight)
								position.set(SHOT_OFFSET_UP).add(sBody.getPosition());
							else
								position.set(SHOT_OFFSET_UP).scl(-1, 1).add(sBody.getPosition());
							velocity.set(0f, SHOT_VEL);
						}
						else if(isFacingRight) {
							position.set(SHOT_OFFSET_RIGHT).add(sBody.getPosition());
							velocity.set(SHOT_VEL, 0f);
						}
						else {
							position.set(SHOT_OFFSET_RIGHT).scl(-1, 1).add(sBody.getPosition());
							velocity.set(-SHOT_VEL, 0f);
						}
						adef = SamusShot.makeSamusShotDef(position, velocity, this);
						agency.createAgent(adef);
						agency.playSound(AudioInfo.Sound.Metroid.SHOOT);
					}
					// stand/run on ground?
					if(sBody.isOnGround()) {
						if(isMoveHorizontal) {
							checkDoStepSound();
							nextMoveState = MoveState.RUN;
						}
						else
							nextMoveState = MoveState.STAND;
					}
					else {
						// fall
						nextMoveState = MoveState.JUMP;
						isLastJumpLandable = false;
						isJumpForceEnabled = false;
					}
				}
				break;
			case JUMP:
			case JUMPSPIN:
				// if body is falling, or is not rising, then enable the next jump and disable jump up force
				if(sBody.getVelocity().y <= UInfo.VEL_EPSILON) {
					isLastJumpLandable = true;
					isJumpForceEnabled = false;
					isJumpSpinAvailable = false;
				}
				// if body is rising but not being advised to jump then disable jump up force
				else if(!advice.jump)
					isJumpForceEnabled = false;

				// If the body is now onground but jump is still advised then disallow jumping until the
				// jump advice stops (to prevent bunny hopping)
				if(sBody.isOnGround() && advice.jump)
					isNextJumpEnabled = false;

				if(sBody.isOnGround() && !isJumpForceEnabled) {
					agency.playSound(AudioInfo.Sound.Metroid.STEP);
					if(isMoveHorizontal)
						nextMoveState = MoveState.RUN;
					else
						nextMoveState = MoveState.STAND;
				}
				else {
					if(curMoveState == MoveState.JUMP) {
						if(sBody.getVelocity().y > 0f && isJumpSpinAvailable && sBody.getPosition().y >
								startJumpY + 2f*UInfo.P2M(UInfo.TILEPIX_Y))
							nextMoveState = MoveState.JUMPSPIN;
						else
							nextMoveState = MoveState.JUMP;
					}
					else {
						// switch to regular JUMP state if moving down and below 1 tile height above jump start position
						if(sBody.getVelocity().y <= 0f &&
								sBody.getPosition().y <= startJumpY + UInfo.P2M(UInfo.TILEPIX_Y))
							nextMoveState = MoveState.JUMP;
						else
							nextMoveState = MoveState.JUMPSPIN;
					}
				}
				break;
		}

		if(nextMoveState == MoveState.JUMP || nextMoveState == MoveState.JUMPSPIN) {
			if(jumpStart)
				sBody.doJumpStart();
			if(isJumpForceEnabled)
				sBody.doJumpContinue(delta);
		}

		if(isMoveHorizontal) {
			switch(nextMoveState) {
				case BALL:
				case STAND:
				case RUN:
					sBody.doGroundMove(advice.moveRight);
					break;
				case JUMP:
					// samus can airmove if jumping and not in damage state
					if(curContactState != ContactState.DAMAGE)
						sBody.doAirMove(advice.moveRight);
					break;
				case JUMPSPIN:
					// samus can airmove while jump spinning even when in damage state
					sBody.doAirMove(advice.moveRight);
					break;
			}
		}
		// if not moving right/left then do stop move
		else {
			if(curContactState == ContactState.DAMAGE && contactStateTimer < 0.2f)
				sBody.doStopMove(true);
			else
				sBody.doStopMove(false);
		}

		sBody.clampMove();

		// if advised move left or right (but not both at same time!) then set facing direction accordingly
		if(advice.moveRight^advice.moveLeft)
			isFacingRight = advice.moveRight;
		moveStateTimer = nextMoveState == curMoveState ? moveStateTimer + delta : 0f;
		curMoveState = nextMoveState;
	}

	private void processSpriteState(float delta) {
		sSprite.update(delta, sBody.getPosition(), curMoveState, isFacingRight, isFacingUp);
	}

	private float lastStepSoundTime = 0f;
	private static final float STEP_SOUND_TIME = 0.167f;
	private void checkDoStepSound() {
		if(curMoveState != MoveState.RUN)
			lastStepSoundTime = 0;
		else if(moveStateTimer - lastStepSoundTime >= STEP_SOUND_TIME) {
			lastStepSoundTime = moveStateTimer;
			agency.playSound(AudioInfo.Sound.Metroid.STEP);
		}
	}

	@Override
	public void draw(Batch batch) {
		// toggle sprite on/off each frame while in contact damage state
		if(curContactState == ContactState.DAMAGE && isDrawnLastFrame)
			isDrawnLastFrame = false;
		else {
			sSprite.draw(batch);
			isDrawnLastFrame = true;
		}
	}

	@Override
	public void setFrameAdvice(Advice advice) {
		this.advice = advice.cpy();
	}

	@Override
	public PowType pollNonCharPowerup() {
		return PowType.NONE;
	}

	@Override
	public boolean isDead() {
		return false;
	}

	@Override
	public boolean isAtLevelEnd() {
		return false;
	}

	@Override
	public float getStateTimer() {
		return moveStateTimer;
	}

	@Override
	public Room getCurrentRoom() {
		return sBody.getCurrentRoom();
	}

	@Override
	public Vector2 getPosition() {
		return sBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return sBody.getVelocity();
	}

	@Override
	public void applyPowerup(PowType pt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		sBody.dispose();
	}
}
