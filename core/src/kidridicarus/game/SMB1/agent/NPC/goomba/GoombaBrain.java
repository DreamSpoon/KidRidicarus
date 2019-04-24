package kidridicarus.game.SMB1.agent.NPC.goomba;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.proactoragent.ProactorAgentBrain;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.HeadBounceGiveAgent;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.SMB1.agentbrain.HeadBounceBrainContactFrameInput;
import kidridicarus.game.info.SMB1_Audio;

public class GoombaBrain extends ProactorAgentBrain {
	private static final float GIVE_DAMAGE = 8f;
	private static final float GOOMBA_SQUISH_TIME = 2f;
	private static final float GOOMBA_BUMP_FALL_TIME = 6f;

	enum MoveState { WALK, FALL, DEAD_BUMP, DEAD_SQUISH;
		public boolean equalsAny(MoveState ...otherStates) {
			for(MoveState state : otherStates) { if(this.equals(state)) return true; } return false;
		}
		public boolean isDead() { return this.equalsAny(DEAD_BUMP, DEAD_SQUISH); }
	}

	private enum DeadState { NONE, BUMP, SQUISH }

	private Goomba parent;
	private GoombaBody body;
	private float moveStateTimer;
	private MoveState moveState;
	private boolean isFacingRight;
	private boolean deadBumpRight;
	private Agent perp;	// perpetrator of squish, bump, and damage
	private DeadState nextDeadState;
	private boolean despawnMe;
	private RoomBox lastKnownRoom;

	public GoombaBrain(Goomba parent, GoombaBody body) {
		this.parent = parent;
		this.body = body;
		moveStateTimer = 0f;
		moveState = MoveState.WALK;
		isFacingRight = false;
		deadBumpRight = false;
		perp = null;
		despawnMe = false;
		nextDeadState = DeadState.NONE;
		lastKnownRoom = null;
	}

	@Override
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		boolean isHeadBounced = false;
		for(Agent agent : ((HeadBounceBrainContactFrameInput) cFrameInput).headBounceBeginContacts) {
			// if they take contact damage and give head bounces...
			if(agent instanceof ContactDmgTakeAgent && agent instanceof HeadBounceGiveAgent) {
				// if can't pull head bounce then try pushing contact damage
				if(((HeadBounceGiveAgent) agent).onGiveHeadBounce(parent)) {
					isHeadBounced = true;
					perp = agent;
				}
				else
					((ContactDmgTakeAgent) agent).onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
			}
			// pull head bounces from head bounce agents
			else if(agent instanceof HeadBounceGiveAgent)
				isHeadBounced = ((HeadBounceGiveAgent) agent).onGiveHeadBounce(parent);
			// push damage to contact damage agents
			else if(agent instanceof ContactDmgTakeAgent)
				((ContactDmgTakeAgent) agent).onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
		}

		if(isHeadBounced)
			nextDeadState = DeadState.SQUISH;
	}

	@Override
	public GoombaSpriteFrameInput processFrame(BrainFrameInput frameInput) {
		processContacts((RoomingBrainFrameInput) frameInput);
		processMove(frameInput.timeDelta);
		return new GoombaSpriteFrameInput(!despawnMe, body.getPosition(),
				!isFacingRight, frameInput.timeDelta, moveState);
	}

	private void processContacts(RoomingBrainFrameInput frameInput) {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!moveState.isDead() && !frameInput.isContactKeepAlive) || frameInput.isContactDespawn)
			despawnMe = true;
		// update last known room if not dead
		if(!moveState.isDead() && frameInput.roomBox != null)
			lastKnownRoom = frameInput.roomBox;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return;
		}

		// if move is blocked by solid or an agent then change facing dir
		if(body.getSpine().isKoopaSideMoveBlocked(isFacingRight, true))
			isFacingRight = !isFacingRight;

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case WALK:
				body.getSpine().doWalkMove(isFacingRight);
				break;
			case FALL:
				break;	// do nothing if falling
			case DEAD_BUMP:
				// new bump?
				if(moveStateChanged)
					startBump();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_BUMP_FALL_TIME)
					parent.getAgency().removeAgent(parent);
				break;
			case DEAD_SQUISH:
				// new squish?
				if(moveStateChanged)
					startSquish();
				// wait a short time and disappear
				else if(moveStateTimer > GOOMBA_SQUISH_TIME)
					parent.getAgency().removeAgent(parent);
				break;
		}

		// do space wrap last so that contacts are maintained
		body.getSpine().checkDoSpaceWrap(lastKnownRoom);

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private MoveState getNextMoveState() {
		if(nextDeadState == DeadState.BUMP)
			return MoveState.DEAD_BUMP;
		else if(nextDeadState == DeadState.SQUISH)
			return MoveState.DEAD_SQUISH;
		else if(body.getSpine().isOnGround())
			return MoveState.WALK;
		else
			return MoveState.FALL;
	}

	private void startSquish() {
		body.getSpine().doDeadSquishContactsAndMove();
		if(perp != null)
			parent.getAgency().createAgent(FloatingPoints.makeAP(100, true, body.getPosition(), perp));
		parent.getAgency().getEar().playSound(SMB1_Audio.Sound.STOMP);
	}

	private void startBump() {
		body.getSpine().doDeadBumpContactsAndMove(deadBumpRight);
		if(perp != null)
			parent.getAgency().createAgent(FloatingPoints.makeAP(100, false, body.getPosition(), perp));
		parent.getAgency().getEar().playSound(SMB1_Audio.Sound.KICK);
	}

	// assume any amount of damage kills, for now...
	public boolean onTakeDamage(Agent agent, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(nextDeadState != DeadState.NONE || !(agent instanceof PlayerAgent))
			return false;

		this.perp = agent;
		nextDeadState = DeadState.BUMP;
		deadBumpRight = body.getSpine().isDeadBumpRight(dmgOrigin);
		return true;
	}

	public void onTakeBump(Agent agent) {
		// no dead bumps
		if(nextDeadState != DeadState.NONE)
			return;
		// save ref to perpetrator Agent, and check for bump right
		this.perp = agent;
		nextDeadState = DeadState.BUMP;
		Vector2 perpPos = AP_Tool.getCenter(perp);
		if(perpPos == null)
			deadBumpRight = false;
		else
			deadBumpRight = body.getSpine().isDeadBumpRight(perpPos);
	}
}
