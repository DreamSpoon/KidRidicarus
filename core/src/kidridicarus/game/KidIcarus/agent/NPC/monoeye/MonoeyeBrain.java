package kidridicarus.game.KidIcarus.agent.NPC.monoeye;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.fullactor.FullActorBrain;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.agent.other.vanishpoof.VanishPoof;
import kidridicarus.game.KidIcarus.agentspine.FlyBallSpine.AxisGoState;
import kidridicarus.game.info.KidIcarusAudio;

public class MonoeyeBrain extends FullActorBrain {
	private static final float GIVE_DAMAGE = 1f;
	private static final int DROP_HEART_COUNT = 5;

	private enum MoveState { FLY, OGLE, DEAD }

	private Monoeye parent;
	private MonoeyeBody body;
	private MoveState moveState;
	private float moveStateTimer;
	private AxisGoState horizGoState;
	private AxisGoState vertGoState;
	private boolean isFacingRight;
	private boolean isDead;
	private boolean despawnMe;
	private PlayerAgent ogleTarget;
	private boolean isTargetRemoved;

	public MonoeyeBrain(Monoeye parent, MonoeyeBody body) {
		this.parent = parent;
		this.body = body;
		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		// Start move right, and
		horizGoState = AxisGoState.VEL_PLUS;
		// move down.
		vertGoState = AxisGoState.VEL_MINUS;
		isFacingRight = true;
		isDead = false;
		despawnMe = false;
		ogleTarget = null;
		isTargetRemoved = false;
	}

	@Override
	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
	}

	@Override
	public SpriteFrameInput processFrame(BrainFrameInput bodyFrame) {
		processContacts((RoomingBrainFrameInput) bodyFrame);
		processMove(bodyFrame.timeDelta);
		// visible if not despawned and not dead
		return new SpriteFrameInput(!despawnMe & !isDead, body.getPosition(), false, !isFacingRight, false);
	}

	private void processContacts(RoomingBrainFrameInput bodyFrame) {
		// if alive and not touching keep alive box, or if touching despawn, then set despawn flag
		if((!isDead && !bodyFrame.isContactKeepAlive) || bodyFrame.isContactDespawn)
			despawnMe = true;
	}

	private void processMove(float delta) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return;
		}

		processGawkers();

		// if a target is being ogled then do horizontal ogle move
		AxisGoState nextHorizGoState;
		AxisGoState nextVertGoState;
		if(ogleTarget != null) {
			nextHorizGoState = getNextHorizontalOgleState();
			nextVertGoState = AxisGoState.VEL_MINUS;
		}
		// otherwise do regular horizontal move
		else {
			nextHorizGoState = getNextAxisGoState(true, horizGoState);
			nextVertGoState = getNextAxisGoState(false, vertGoState);
		}

		// facing direction depends upon move direction
		isFacingRight = nextHorizGoState.isPlus();

		MoveState nextMoveState = getNextMoveState();
		boolean moveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				break;
			case OGLE:
				// if no ogle target then continue whatever move was already happening
				if(ogleTarget == null)
					break;
				// if changed to plus acceleration then set left accel zone tile and reset right accel zone tile
				if(nextHorizGoState == AxisGoState.ACCEL_PLUS && horizGoState != AxisGoState.ACCEL_PLUS) {
					body.getSpine().setLeftFlyBoundToCurrentX();
					body.getSpine().resetRightFlyBound();
				}
				// if changed to minus acceleration then set right accel zone tile and reset left accel zone tile
				else if(nextHorizGoState == AxisGoState.ACCEL_MINUS && horizGoState != AxisGoState.ACCEL_MINUS) {
					body.getSpine().setRightFlyBoundToCurrentX();
					body.getSpine().resetLeftFlyBound();
				}
				break;
			case DEAD:
				parent.getAgency().createAgent(VanishPoof.makeAP(body.getPosition(), true));
				parent.getAgency().createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
				parent.getAgency().removeAgent(parent);
				parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		horizGoState = nextHorizGoState;
		vertGoState = nextVertGoState;
		body.getSpine().applyAxisMoves(horizGoState, vertGoState);

		moveStateTimer = moveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
	}

	private AxisGoState getNextHorizontalOgleState() {
		// if accelerating right
		if(horizGoState == AxisGoState.ACCEL_PLUS) {
			// change to velocity in same direction after acceleration is finished
			return body.getSpine().isContinueAcceleration(true, true) ?
					AxisGoState.ACCEL_PLUS : AxisGoState.VEL_PLUS;
		}
		// if moving right
		else if(horizGoState == AxisGoState.VEL_PLUS) {
			// if target is on left then change direction
			return body.getSpine().isTargetOnSide(ogleTarget, false) ?
					AxisGoState.ACCEL_MINUS : AxisGoState.VEL_PLUS;
		}
		// if accelerating left
		else if(horizGoState == AxisGoState.ACCEL_MINUS) {
			// change to velocity in same direction after acceleration is finished
			return body.getSpine().isContinueAcceleration(true, false) ?
					AxisGoState.ACCEL_MINUS : AxisGoState.VEL_MINUS;
		}
		// else moving left
		else {
			// if target is on right then change direction
			return body.getSpine().isTargetOnSide(ogleTarget, true) ?
					AxisGoState.ACCEL_PLUS : AxisGoState.VEL_MINUS;
		}
	}

	private AxisGoState getNextAxisGoState(boolean isHorizontal, AxisGoState currentGoState) {
		if(currentGoState == AxisGoState.ACCEL_PLUS) {
			if(body.getSpine().isContinueAcceleration(isHorizontal, true))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.VEL_PLUS) {
			if(body.getSpine().isChangeDirection(isHorizontal, true))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_PLUS;
		}
		else if(currentGoState == AxisGoState.ACCEL_MINUS) {
			if(body.getSpine().isContinueAcceleration(isHorizontal, false))
				return AxisGoState.ACCEL_MINUS;
			else
				return AxisGoState.VEL_MINUS;
		}
		// VEL_MINUS
		else {
			if(body.getSpine().isChangeDirection(isHorizontal, false))
				return AxisGoState.ACCEL_PLUS;
			else
				return AxisGoState.VEL_MINUS;
		}
	}

	private void processGawkers() {
		// if the target has been removed then de-target
		if(isTargetRemoved) {
			isTargetRemoved = false;
			ogleTarget = null;
		}
		// lose target if moving up
		if(vertGoState.isPlus()) {
			ogleTarget = null;
			return;
		}
		// exit if an ogle target already exists
		if(ogleTarget != null)
			return;

		ogleTarget = body.getSpine().getGawker(isFacingRight);
		if(ogleTarget != null) {
			isTargetRemoved = false;
			// add an AgentRemoveListener to allow de-targeting on death of target
			parent.getAgency().addAgentRemoveListener(new AgentRemoveListener(parent, ogleTarget) {
					@Override
					public void removedAgent() { isTargetRemoved = true; }
				});
		}
	}

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else if(moveState == MoveState.OGLE || ogleTarget != null)
			return MoveState.OGLE;
		else
			return MoveState.FLY;
	}

	// assume any amount of damage kills, for now...
	public boolean onTakeDamage(Agent agent) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}

}
