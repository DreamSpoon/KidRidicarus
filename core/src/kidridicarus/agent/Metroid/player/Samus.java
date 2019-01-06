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

public class Samus extends Agent implements AdvisableAgent, PlayerAgent {
	private static final float MIN_JUMPING_UP_VEL = 0.0001f;
	private static final float DAMAGE_INV_TIME = 0.8f;
	private static final Vector2 DAMAGE_KICK_SIDE_IMP = new Vector2(1.8f, 0f);
	private static final Vector2 DAMAGE_KICK_UP_IMP = new Vector2(0f, 1.3f);

	public enum ContactState { REGULAR, DAMAGE };
	public enum MoveState { STAND, RUN, JUMP, JUMPSPIN };

	private Advice advice;
	private SamusBody sBody;
	private SamusSprite sSprite;

	private MoveState curMoveState;
	private float moveStateTimer;
	private ContactState curContactState;
	private float contactStateTimer;

	private boolean isFacingRight;
	private boolean isJumpForceEnabled;
	// the last jump must land before the next jump can start
	private boolean isLastJumpLanded;
	private boolean isNextJumpEnabled;
	private Vector2 damagePos = new Vector2();
	private boolean isDrawnLastFrame;

	public Samus(Agency agency, AgentDef adef) {
		super(agency, adef);

		advice = new Advice();
		curMoveState = MoveState.STAND;
		moveStateTimer = 0f;
		curContactState = ContactState.REGULAR;
		contactStateTimer = 0f;
		isFacingRight = true;
		isJumpForceEnabled = true;
		isLastJumpLanded = true;
		isNextJumpEnabled = true;
		isDrawnLastFrame = false;

		sBody = new SamusBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		sSprite = new SamusSprite(agency.getAtlas(), sBody.getPosition());

		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		processContacts(delta);
		processMoveState(delta);
		sSprite.update(delta, sBody.getPosition(), curMoveState, isFacingRight);
		advice.clear();
	}

	private void processContacts(float delta) {
		ContactState nextState = ContactState.REGULAR;
		switch(curContactState) {
			case REGULAR:
				for(Agent a : sBody.getContactsByClass(ContactDmgAgent.class)) {
					ContactDmgAgent cda = (ContactDmgAgent) a;
					if(cda.isContactDamage()) {
						nextState = ContactState.DAMAGE;
						doContactDamage(a.getPosition());
					}
				}
				break;
			case DAMAGE:
				if(contactStateTimer > DAMAGE_INV_TIME)
					nextState = ContactState.REGULAR;
				else
					nextState = ContactState.DAMAGE;
				break;
		}

		contactStateTimer = nextState == curContactState ? contactStateTimer + delta : 0f;
		curContactState = nextState;
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
	}

	private void processMoveState(float delta) {
		MoveState nextState = null;
		boolean jumpStart = false;
		// XOR the moveleft and moveright, becuase can't move left and right at same time
		boolean isMoveHorizontal = advice.moveRight^advice.moveLeft;

		switch(curMoveState) {
			case STAND:
			case RUN:
				if(!isNextJumpEnabled && !advice.jump)
					isNextJumpEnabled = true;

				// if body is falling, or is not rising, then enable the next jump
				if(sBody.getVelocity().y <= MIN_JUMPING_UP_VEL)
					isLastJumpLanded = true;

				if(advice.jump && sBody.isOnGround() && isLastJumpLanded && isNextJumpEnabled) {
					isLastJumpLanded = false;
					isJumpForceEnabled = true;
					jumpStart = true;
					if(isMoveHorizontal)
						nextState = MoveState.JUMPSPIN;
					else
						nextState = MoveState.JUMP;
				}
				else {
					if(isMoveHorizontal)
						nextState = MoveState.RUN;
					else
						nextState = MoveState.STAND;
				}
				break;
			case JUMP:
			case JUMPSPIN:
				// if body is falling, or is not rising, then enable the next jump and disable jump up force
				if(sBody.getVelocity().y <= MIN_JUMPING_UP_VEL) {
					isLastJumpLanded = true;
					isJumpForceEnabled = false;
				}
				// if body is rising but not being advised to jump then disable jump up force
				else if(!advice.jump)
					isJumpForceEnabled = false;

				// If the body is now onground but jump is still advised then disallow jumping until the
				// jump advice stops
				if(sBody.isOnGround() && advice.jump)
					isNextJumpEnabled = false;

				if(sBody.isOnGround() && !isJumpForceEnabled) {
					if(isMoveHorizontal)
						nextState = MoveState.RUN;
					else
						nextState = MoveState.STAND;
				}
				else
					nextState = curMoveState;
				break;
		}

		if(nextState == MoveState.JUMP || nextState == MoveState.JUMPSPIN) {
			if(jumpStart)
				sBody.doJumpStart();
			if(isJumpForceEnabled)
				sBody.doJumpContinue(delta);
		}

		if(isMoveHorizontal) {
			switch(nextState) {
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

		moveStateTimer = nextState == curMoveState ? moveStateTimer + delta : 0f;
		curMoveState = nextState;
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
