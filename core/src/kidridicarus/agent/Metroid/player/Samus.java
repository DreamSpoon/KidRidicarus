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
	private static final Vector2 SHOT_OFFSET_RIGHT = UInfo.P2MVector(11, 7);
	private static final Vector2 SHOT_OFFSET_UP = UInfo.P2MVector(1, 20);
	private static final float SHOT_VEL = 2f;
	private static final float SHOOT_COOLDOWN = 0.15f;
	private static final float JUMPSHOOT_RESPIN_DELAY = 0.05f;
	private static final float STEP_SOUND_TIME = 0.167f;
	private static final float POSTPONE_RUN_DELAY = 0.15f;

	public enum ContactState { REGULAR, DAMAGE };
	public enum MoveState { STAND, RUN, JUMP, JUMPSPIN, BALL, JUMPSHOOT, SHOOT };

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
	private float shootCooldownTime;
	private float startJumpY;
	private boolean isJumpSpinAvailable;
	private float lastStepSoundTime = 0f;
	private boolean isAutoContinueRightAirMove;
	private boolean isAutoContinueLeftAirMove;

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
		shootCooldownTime = agency.getGlobalTimer();
		startJumpY = 0;
		isJumpSpinAvailable = false;
		isAutoContinueRightAirMove = false;
		isAutoContinueLeftAirMove = false;

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
			case SHOOT:
				if(!isNextJumpEnabled && !advice.jump)
					isNextJumpEnabled = true;

				// if body is falling, or is not rising, then enable the next jump
				if(sBody.getVelocity().y <= UInfo.VEL_EPSILON)
					isLastJumpLandable = true;

				isFacingUp = advice.moveUp;

				// switch to ball form?
				if(advice.moveDown && sBody.isOnGround()) {
					sBody.switchToBallForm();
					nextMoveState = MoveState.BALL;
				}
				// jump?
				else if(advice.jump && sBody.isOnGround() && isLastJumpLandable && isNextJumpEnabled) {
					isLastJumpLandable = false;
					isJumpForceEnabled = true;
					jumpStart = true;
					if(isMoveHorizontal)
						isJumpSpinAvailable = true;
					startJumpY = sBody.getPosition().y;
					agency.playSound(AudioInfo.Sound.Metroid.JUMP);
					nextMoveState = MoveState.JUMP;
				}
				else {
					boolean didShoot = checkAndDoShoot();

					// stand/run on ground?
					if(sBody.isOnGround()) {
						// moving sideways?
						if(isMoveHorizontal) {
							if((advice.moveRight && sBody.isContactingWall(true)) ||
									(advice.moveLeft && sBody.isContactingWall(false)))
								nextMoveState = MoveState.STAND;
							// move is not blocked
							else {
								checkDoStepSound();
								// Start the shoot state if did shoot, or postpone the run state and continue the
								// shoot state if still in the postpone period 
								if(didShoot || (curMoveState == MoveState.SHOOT &&
										moveStateTimer < POSTPONE_RUN_DELAY)) {
									nextMoveState = MoveState.SHOOT;
								}
								else
									nextMoveState = MoveState.RUN;
							}
						}
						// on ground and not moving, therefore stand state
						else
							nextMoveState = MoveState.STAND;
					}
					// fall
					else {
						// jump is disable when fall starts
						isLastJumpLandable = false;
						isJumpForceEnabled = false;

						if(didShoot)
							nextMoveState = MoveState.JUMPSHOOT;
						else
							nextMoveState = MoveState.JUMP;
					}
				}
				break;
			case JUMP:
			case JUMPSPIN:
			case JUMPSHOOT:
				// if body is falling then enable the next jump and disable jump up force
				if(sBody.getVelocity().y <= UInfo.VEL_EPSILON) {
					isLastJumpLandable = true;
					isJumpForceEnabled = false;
				}
				// else if body is not being advised to jump then disable jump up force
				else if(!advice.jump)
					isJumpForceEnabled = false;

				// If the body is now onground but jump is still advised then disallow jumping until the
				// jump advice stops (to prevent bunny hopping)
				if(sBody.isOnGround() && advice.jump)
					isNextJumpEnabled = false;

				// disallow change to facing up if player is falling and they started jump with spin allowed
				if(!isFacingUp && advice.moveUp && !(isJumpSpinAvailable && sBody.getVelocity().y <= 0f))
					isFacingUp = true;

				// Change back to facing sideways if already facing up, and not advised to moveUp,
				// and jump spin is available, and body is moving upward
				if(isFacingUp && !advice.moveUp && isJumpSpinAvailable && sBody.getVelocity().y > 0f)
					isFacingUp = false;

				// check for landing on ground
				if(sBody.isOnGround() && !isJumpForceEnabled) {
					isJumpSpinAvailable = false;
					isAutoContinueLeftAirMove = false;
					isAutoContinueRightAirMove = false;
					// play landing sound
					agency.playSound(AudioInfo.Sound.Metroid.STEP);
					// if on ground and moving then run
					if(isMoveHorizontal)
						nextMoveState = MoveState.RUN;
					else
						nextMoveState = MoveState.STAND;
				}
				// still mid-air
				else {
					// do shoot?
					if(checkAndDoShoot())
						nextMoveState = MoveState.JUMPSHOOT;
					// already shooting?
					else if(curMoveState == MoveState.JUMPSHOOT) {
						// If delay is finished, and not advised to shoot, and moving horizontally, and spin
						// is available, and body is moving upward, and samus is at least 2 tiles higher than
						// jump start position, then switch back to jumpspin.
						if(moveStateTimer > JUMPSHOOT_RESPIN_DELAY && !advice.shoot && isMoveHorizontal &&
								isJumpSpinAvailable && sBody.getVelocity().y > 0f &&
								sBody.getPosition().y > startJumpY + 2f*UInfo.P2M(UInfo.TILEPIX_Y)) {
							nextMoveState = MoveState.JUMPSPIN;
						}
						// otherwise continue jump shoot
						else
							nextMoveState = MoveState.JUMPSHOOT;
					}
					// not shooting, just jumping?
					else if(curMoveState == MoveState.JUMP) {
						// switch to jumpspin when samus moves at least 2 tiles higher than jump start position 
						if(sBody.getVelocity().y > 0f && isJumpSpinAvailable && sBody.getPosition().y >
								startJumpY + 2f*UInfo.P2M(UInfo.TILEPIX_Y)) {
							// If move left/right advice is given during change from jump to jumpspin then
							// auto continue air move in advised direction until user presses other direction
							if(advice.moveRight)
								isAutoContinueRightAirMove = true;
							else if(advice.moveLeft)
								isAutoContinueLeftAirMove = true;

							nextMoveState = MoveState.JUMPSPIN;
						}
						else
							nextMoveState = MoveState.JUMP;
					}
					// not shooting, currently jump spinning
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

		// check for and apply vertical (jump) impulses, forces, etc.
		if(nextMoveState == MoveState.JUMP || nextMoveState == MoveState.JUMPSPIN ||
				nextMoveState == MoveState.JUMPSHOOT) {
			if(jumpStart)
				sBody.doJumpStart();
			if(isJumpForceEnabled)
				sBody.doJumpContinue(delta);
		}

		// check for and apply horizontal movement impulses, forces, etc.
		if(isMoveHorizontal) {
			// cancel auto air move right?
			if(isAutoContinueRightAirMove && advice.moveLeft)
				isAutoContinueRightAirMove = false;
			// cancel auto air move left?
			else if(isAutoContinueLeftAirMove && advice.moveRight)
				isAutoContinueLeftAirMove = false;

			switch(nextMoveState) {
				case BALL:
				case STAND:
				case RUN:
				case SHOOT:
					sBody.doGroundMove(advice.moveRight);
					break;
				case JUMP:
				case JUMPSHOOT:
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
		// not moving left/right - may be on-ground or mid-air
		else {
			// The spin jump may cause air move to continue even though move left/right advice is not given,
			// check left...
			if(isAutoContinueRightAirMove)
				sBody.doAirMove(true);
			// ... and right
			// Note: Auto left/right air move is cancelled when on-ground.
			else if(isAutoContinueLeftAirMove)
				sBody.doAirMove(false);
			// if auto move is not continuing, then do stop move
			else {
				if(curContactState == ContactState.DAMAGE && contactStateTimer < 0.2f)
					sBody.doStopMove(true);
				else
					sBody.doStopMove(false);
			}
		}

		// check against maximum velocities, etc.
		sBody.clampMove();

		// update prev velocity
		sBody.postUpdate();

		// if advised move left or right (but not both at same time!) then set facing direction accordingly
		if(advice.moveRight^advice.moveLeft)
			isFacingRight = advice.moveRight;

		// update move state timer and current move state
		moveStateTimer = nextMoveState == curMoveState ? moveStateTimer + delta : 0f;
		curMoveState = nextMoveState;
	}

	private void processSpriteState(float delta) {
		sSprite.update(delta, sBody.getPosition(), curMoveState, isFacingRight, isFacingUp);
	}

	private void checkDoStepSound() {
		if(curMoveState != MoveState.RUN)
			lastStepSoundTime = 0;
		else if(moveStateTimer - lastStepSoundTime >= STEP_SOUND_TIME) {
			lastStepSoundTime = moveStateTimer;
			agency.playSound(AudioInfo.Sound.Metroid.STEP);
		}
	}

	private boolean checkAndDoShoot() {
		// can't shoot if not advised to shoot or cooldown has not finished
		if(!advice.shoot || shootCooldownTime > agency.getGlobalTimer())
			return false;

		shootCooldownTime = agency.getGlobalTimer() + SHOOT_COOLDOWN;

		// calculate position and velocity of shot based on samus' orientation
		Vector2 velocity = new Vector2();
		Vector2 position = new Vector2();
		if(isFacingUp) {
			velocity.set(0f, SHOT_VEL);
			if(isFacingRight)
				position.set(SHOT_OFFSET_UP).add(sBody.getPosition());
			else
				position.set(SHOT_OFFSET_UP).scl(-1, 1).add(sBody.getPosition());
		}
		else if(isFacingRight) {
			velocity.set(SHOT_VEL, 0f);
			position.set(SHOT_OFFSET_RIGHT).add(sBody.getPosition());
		}
		else {
			velocity.set(-SHOT_VEL, 0f);
			position.set(SHOT_OFFSET_RIGHT).scl(-1, 1).add(sBody.getPosition());
		}
		// create shot
		AgentDef adef = SamusShot.makeSamusShotDef(position, velocity, this);
		agency.createAgent(adef);
		agency.playSound(AudioInfo.Sound.Metroid.SHOOT);

		// shot fired, so return true
		return true;
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
