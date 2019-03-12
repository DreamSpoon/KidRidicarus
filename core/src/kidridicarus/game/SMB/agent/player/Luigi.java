package kidridicarus.game.SMB.agent.player;

import java.util.HashSet;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.AgentSupervisor;
import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.SMB.agentbody.player.LuigiBody;
import kidridicarus.game.SMB.agentsprite.player.LuigiSprite;
import kidridicarus.game.tool.QQ;

public class Luigi extends Agent implements PlayerAgent, DisposableAgent {
	private static final Vector2 DUCK_OFFSET = new Vector2(0f, UInfo.P2M(7f));

	public enum PowerState {
			SMALL, BIG, FIRE;
			public boolean isBigBody() { return !this.equals(SMALL); }
		}
	public enum MoveState { STAND, RUN, BRAKE, FALL, DUCK, DUCKFALL, DUCKJUMP, JUMP;
			public boolean equalsAny(MoveState ...statesInput) {
				for(MoveState state : statesInput) if(this.equals(state)) return true;
				return false;
			}

			public boolean isDuckType() {
				return this.equalsAny(DUCK, DUCKFALL, DUCKJUMP);
			}

			public boolean isJumpType() {
				return this.equalsAny(JUMP, DUCKJUMP);
			}

			public boolean isGroundType() {
				return this.equalsAny(STAND, RUN, BRAKE);
			}
		}

	private LuigiSupervisor supervisor;
	private LuigiObserver observer;
	private LuigiBody body;
	private LuigiSprite sprite;

	private MoveState moveState;
	private float moveStateTimer;
	private PowerState powerState;
	private boolean facingRight;
	private boolean isNextJumpAllowed;
	private boolean isNextJumpDelayed;

	public Luigi(Agency agency, ObjectProperties properties) {
		super(agency, properties);
QQ.pr("you made Luigi so happy!");
		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		facingRight = true;
		isNextJumpAllowed = false;
		isNextJumpDelayed = false;
		powerState = PowerState.BIG;

		body = new LuigiBody(this, agency.getWorld(), Agent.getStartPoint(properties), powerState.isBigBody(), false);
		sprite = new LuigiSprite(agency.getAtlas(), body.getPosition(), powerState, facingRight);
		observer = new LuigiObserver(this);
		supervisor = new LuigiSupervisor(this);
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doUpdate(float delta) {
		processMove(delta, supervisor.pollMoveAdvice());
		processSprite(delta);
	}

	private void processMove(float delta, MoveAdvice moveAdvice) {
		Direction4 moveDir = getLuigiMoveDir(moveAdvice);
		boolean onGround = body.getSpine().isOnGround();
		boolean doHorizontalImpulse = false;
		boolean doDecelImpulse = false;
		MoveState nextMoveState = getNextMoveState(moveAdvice);
		// check for body size change due to duck state change
		switch(nextMoveState) {
			case DUCK:
			case DUCKFALL:
			case DUCKJUMP:
				// if the move state changed to duck then change body to ducking body
				if(!moveState.isDuckType())
					body.defineBody(body.getPosition().cpy().sub(DUCK_OFFSET), powerState.isBigBody(), true);
				break;
			case STAND:
			case RUN:
			case BRAKE:
			case FALL:
			case JUMP:
				// if the move state was duck then change body to regular body
				if(moveState.isDuckType())
					body.defineBody(body.getPosition().cpy().add(DUCK_OFFSET), powerState.isBigBody(), false);
				break;
		}

		// If current move type is air and next move type is ground and advised to jump then delay jump until
		// jump advice is released.
		if(!moveState.isGroundType() && nextMoveState.isGroundType() && moveAdvice.action1)
			isNextJumpDelayed = true;

		if(onGround && !body.getSpine().isMovingUp()) {
			isNextJumpAllowed = true;
			if(!moveAdvice.action1)
				isNextJumpDelayed = false;
		}

		// do other changes
		switch(nextMoveState) {
			case STAND:
				if(moveDir.isHorizontal())
					doHorizontalImpulse = true;
				else
					doDecelImpulse = true;
				break;
			case RUN:
				if(moveDir.isHorizontal())
					doHorizontalImpulse = true;
				else
					doDecelImpulse = true;
				break;
			case BRAKE:
				doDecelImpulse = true;
				break;
			case FALL:
				break;
			case DUCK:
			case DUCKFALL:
				break;
			case JUMP:
			case DUCKJUMP:
				if(moveState != nextMoveState) {
					isNextJumpAllowed = false;
					body.getSpine().applyJumpImpulse();
				}
				break;
		}

		if(moveDir.isHorizontal()) {
			if(onGround && !moveState.isDuckType()) {
				// check for change of facing direction
				if(moveDir == Direction4.RIGHT)
					facingRight = true;
				else if(moveDir == Direction4.LEFT)
					facingRight = false;
			}
		}

		if(doHorizontalImpulse)
			body.getSpine().applyWalkMove(facingRight, moveAdvice.action0);
		if(doDecelImpulse)
			body.getSpine().applyDecelMove(facingRight);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState(MoveAdvice moveAdvice) {
		if(body.getSpine().isOnGround())
			return getNextMoveStateGround(moveAdvice);
		else
			return getNextMoveStateAir(moveAdvice);
	}

	private MoveState getNextMoveStateGround(MoveAdvice moveAdvice) {
		Direction4 moveDir = getLuigiMoveDir(moveAdvice);

		// if advised to jump and jumping is okay...
		if(moveAdvice.action1 && isNextJumpAllowed && !isNextJumpDelayed) {
			// if ducking already then duck jump
			if(moveState.isDuckType())
				return MoveState.DUCKJUMP;
			// otherwise regular jump
			else
				return MoveState.JUMP;
		}
		// if big body mario and move down is advised then duck
		else if(powerState.isBigBody() && moveDir == Direction4.DOWN)
			return MoveState.DUCK;
		// moving too slowly?
		else if(body.getSpine().isStandingStill())
			return MoveState.STAND;
		// moving in wrong direction?
		else if(body.getSpine().isBraking(facingRight))
			return MoveState.BRAKE;
		else
			return MoveState.RUN;
	}

	private MoveState getNextMoveStateAir(MoveAdvice moveAdvice) {
		// if in jump state then continue jump state
		if(moveState.isJumpType())
			return moveState;
		// not in jump state
		else {
			// if is ducking then do duck fall 
			if(moveState.isDuckType())
				return MoveState.DUCKFALL;
			// not ducking so do regular fall
			else
				return MoveState.FALL;
		}
	}

	private Direction4 getLuigiMoveDir(MoveAdvice moveAdvice) {
		// if no left/right move then return unmodified direction from move advice
		if(moveAdvice.moveLeft^moveAdvice.moveRight == false) {
			// down takes priority over up advice
			if(moveAdvice.moveDown)
				return Direction4.DOWN;
			else if(moveAdvice.moveUp)
				return Direction4.UP;
			else
				return Direction4.NONE;
		}

		// if advising move down while advising left/right then return no direction
		if(moveAdvice.moveDown)
			return Direction4.NONE;
		// ignore move up advice and return horizontal move direction
		else {
			if(moveAdvice.moveRight)
				return Direction4.RIGHT;
			else
				return Direction4.LEFT;
		}
	}

	private void processSprite(float delta) {
		sprite.update(delta, body.getPosition(), moveState, powerState, facingRight);
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(sprite);
	}

	@Override
	public AgentSupervisor getSupervisor() {
		return supervisor;
	}

	@Override
	public GameAgentObserver getObserver() {
		return observer;
	}

	@Override
	public Room getCurrentRoom() {
		return body.getSpine().getCurrentRoom();
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	// unchecked cast to T warnings ignored because T is checked with class.equals(cls) 
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key, Object defaultValue, Class<T> cls) {
		if(key.equals(CommonKV.Script.KEY_FACINGRIGHT) && Boolean.class.equals(cls)) {
			Boolean he = facingRight;
			return (T) he;
		}
		return super.getProperty(key, defaultValue, cls);
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
