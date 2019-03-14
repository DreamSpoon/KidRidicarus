package kidridicarus.game.agent.SMB.player.luigi;

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
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.agent.SMB.other.bumptile.BumpTile.TileBumpStrength;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.tool.QQ;

public class Luigi extends Agent implements PlayerAgent, DisposableAgent {
	private static final Vector2 DUCK_OFFSET = new Vector2(0f, UInfo.P2M(7f));
	private static final float DEAD_DELAY_TIME = 3f;

	public enum PowerState {
		SMALL, BIG, FIRE;
		public boolean isBigBody() { return !this.equals(SMALL); }
	}
	public enum MoveState {
		STAND, RUN, BRAKE, FALL, DUCK, DUCKFALL, DUCKJUMP, JUMP, DEAD;
		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isDuck() { return this.equalsAny(DUCK, DUCKFALL, DUCKJUMP); }
		public boolean isJump() { return this.equalsAny(JUMP, DUCKJUMP); }
		public boolean isOnGround() { return this.equalsAny(STAND, RUN, BRAKE, DUCK); }
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
	private boolean isJumpForceContinue;

	private boolean isHeadBumped;

	public Luigi(Agency agency, ObjectProperties properties) {
		super(agency, properties);
QQ.pr("you made Luigi so happy!");
		moveState = MoveState.STAND;
		moveStateTimer = 0f;
		facingRight = true;
		isNextJumpAllowed = false;
		isNextJumpDelayed = false;
		isJumpForceContinue = false;
		isHeadBumped = false;
		powerState = PowerState.FIRE;

		body = new LuigiBody(this, agency.getWorld(), Agent.getStartPoint(properties), new Vector2(0f, 0f),
				powerState.isBigBody(), false);
		sprite = new LuigiSprite(agency.getAtlas(), body.getPosition(), powerState, facingRight);
		observer = new LuigiObserver(this);
		supervisor = new LuigiSupervisor(this);
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doUpdate(delta); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.POST_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doPostUpdate(); }
		});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_TOP, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	// check for and do head bumps
	private void doContactUpdate() {
		// exit if head bump flag hasn't reset
		if(isHeadBumped)
			return;
		// if luigi is big then hit hard
		if(powerState.isBigBody())
			isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.HARD);
		else
			isHeadBumped = body.getSpine().checkDoHeadBump(TileBumpStrength.SOFT);
	}

	private void doUpdate(float delta) {
		processContacts();
		processMove(delta, supervisor.pollMoveAdvice());
		processSprite(delta);
	}

	private void processContacts() {
		// if head bump flag is on, and body is moving down, then reset the flag
		if(isHeadBumped && body.getSpine().isMovingDown())
			isHeadBumped = false;
	}

	private void processMove(float delta, MoveAdvice moveAdvice) {
		MoveState nextMoveState = getNextMoveState(moveAdvice);
		// if luigi is dead...
		if(nextMoveState == MoveState.DEAD) {
			// and if newly dead then disable contacts and start dead sound
			if(moveState != nextMoveState) {
				body.disableAllContacts();
				observer.stopAllMusic();
				agency.playSound(AudioInfo.Sound.SMB.MARIO_DIE);
			}
			// ... and if died a long time ago then do game over
			else if(moveStateTimer > DEAD_DELAY_TIME)
				supervisor.setGameOver();
			// otherwise do nothing
		}
		else if(nextMoveState.isOnGround())
			processGroundMove(moveAdvice, nextMoveState);
		else
			processAirMove(moveAdvice, nextMoveState);

		moveStateTimer = moveState == nextMoveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private void processGroundMove(MoveAdvice moveAdvice, MoveState nextMoveState) {
		// if was not ducking and is now ducking then define ducking body
		if(!moveState.isDuck() && nextMoveState.isDuck()) {
			body.defineBody(body.getPosition().cpy().sub(DUCK_OFFSET), body.getVelocity(),
					powerState.isBigBody(), true);
		}
		// if was ducking and is now now ducking then define regular body
		else if(moveState.isDuck() && !nextMoveState.isDuck()) {
			body.defineBody(body.getPosition().cpy().add(DUCK_OFFSET), body.getVelocity(),
					powerState.isBigBody(), false);
		}

		// If current move type is air and next move type is ground and jump is advised then delay jump until
		// jump advice is released.
		if(!moveState.isOnGround() && nextMoveState.isOnGround() && moveAdvice.action1)
			isNextJumpDelayed = true;

		// if not moving up (i.e. resting on ground) then allow next jump...
		if(!body.getSpine().isMovingUp()) {
			isNextJumpAllowed = true;
			// ... and if jump advice is released then turn off jump delay
			if(!moveAdvice.action1)
				isNextJumpDelayed = false;
		}

		// do other ground move changes
		Direction4 moveDir = getLuigiMoveDir(moveAdvice);
		boolean doHorizontalImpulse = false;
		boolean doDecelImpulse = false;
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
			case DUCK:
				doDecelImpulse = true;
				break;
			default:
				throw new IllegalStateException("Only ground move states allowed, wrong nextMoveState="+nextMoveState);
		}

		// if not ducking then apply move advice to facing direction
		if(moveDir.isHorizontal() && !moveState.isDuck()) {
			if(moveDir == Direction4.RIGHT)
				facingRight = true;
			else if(moveDir == Direction4.LEFT)
				facingRight = false;
		}

		if(doHorizontalImpulse)
			body.getSpine().applyWalkMove(facingRight, moveAdvice.action0);
		if(doDecelImpulse)
			body.getSpine().applyDecelMove(facingRight, nextMoveState.isDuck());
	}

	private void processAirMove(MoveAdvice moveAdvice, MoveState nextMoveState) {
		if(!body.getSpine().isMovingUp())
			isJumpForceContinue = false;

		boolean isMoveStateChange = (moveState != nextMoveState);
		switch(nextMoveState) {
			case JUMP:
			case DUCKJUMP:
				if(isMoveStateChange) {
					isNextJumpAllowed = false;
					isJumpForceContinue = true;
					body.getSpine().applyJumpImpulse();
				}
				else {
					if(!moveAdvice.action1)
						isJumpForceContinue = false;
				}
				break;
			case FALL:
			case DUCKFALL:
				break;
			default:
				throw new IllegalStateException("Only air move states allowed, wrong nextMoveState="+nextMoveState);
		}

		// luigi might be ducking and moving right/left
		if(moveAdvice.moveRight && !moveAdvice.moveLeft)
			body.getSpine().applyAirMove(true);
		else if(moveAdvice.moveLeft && !moveAdvice.moveRight)
			body.getSpine().applyAirMove(false);

		// if jump force must continue then apply it
		if(isJumpForceContinue) {
			if(isMoveStateChange)
				body.getSpine().applyJumpForce(0f);
			else
				body.getSpine().applyJumpForce(moveStateTimer);
		}

		body.getSpine().capFallVelocity();
	}

	private MoveState getNextMoveState(MoveAdvice moveAdvice) {
		// if luigi is already dead, or hit a despawn box, then return dead
		if(moveState == MoveState.DEAD || body.getSpine().isContactDespawn())
			return MoveState.DEAD;
		// if on ground then do ground move
		else if(body.getSpine().isOnGround())
			return getNextMoveStateGround(moveAdvice);
		// do air move
		else
			return getNextMoveStateAir(moveAdvice);
	}

	private MoveState getNextMoveStateGround(MoveAdvice moveAdvice) {
		Direction4 moveDir = getLuigiMoveDir(moveAdvice);

		// if current state is an air state and body is moving up then continue air state
		if(!moveState.isOnGround() && body.getSpine().isMovingUp())
			return moveState;
		// if advised to jump and jumping is okay...
		else if(moveAdvice.action1 && isNextJumpAllowed && !isNextJumpDelayed) {
			// if ducking already then duck jump
			if(moveState.isDuck())
				return MoveState.DUCKJUMP;
			// otherwise regular jump
			else
				return MoveState.JUMP;
		}
		// if big body luigi and move down is advised then duck
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
		if(moveState.isJump())
			return moveState;
		// not in jump state
		else {
			// if is ducking then do duck fall 
			if(moveState.isDuck())
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

	private void doPostUpdate() {
		// let body update previous velocity
		body.postUpdate();
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
	public RoomBox getCurrentRoom() {
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
