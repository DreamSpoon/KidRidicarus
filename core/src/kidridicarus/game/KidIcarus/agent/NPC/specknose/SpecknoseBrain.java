package kidridicarus.game.KidIcarus.agent.NPC.specknose;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.proactoragent.ProactorAgentBrain;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.agent.other.vanishpoof.VanishPoof;
import kidridicarus.game.KidIcarus.agentspine.FlyBallSpine.AxisGoState;
import kidridicarus.game.info.KidIcarusAudio;

public class SpecknoseBrain extends ProactorAgentBrain {
	private static final float GIVE_DAMAGE = 1f;
	private static final int DROP_HEART_COUNT = 10;
	private static final float HORIZONTAL_ONLY_CHANCE = 1/6f;

	private enum MoveState { FLY, DEAD }

	private Specknose parent;
	private SpecknoseBody body;
	private MoveState moveState;
	private float moveStateTimer;
	private AxisGoState horizGoState;
	private AxisGoState vertGoState;
	private boolean isHorizontalOnly;
	private boolean isDead;
	private boolean despawnMe;

	public SpecknoseBrain(Specknose parent, SpecknoseBody body) {
		this.parent = parent;
		this.body = body;
		moveStateTimer = 0f;
		moveState = MoveState.FLY;
		// Start move right, and
		if(Math.random() <= 0.5f)
			horizGoState = AxisGoState.VEL_PLUS;
		else
			horizGoState = AxisGoState.VEL_MINUS;
		// move down.
		vertGoState = AxisGoState.VEL_MINUS;
		isDead = false;
		despawnMe = false;
		isHorizontalOnly = Math.random() <= HORIZONTAL_ONLY_CHANCE;
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
		return new AnimSpriteFrameInput(!despawnMe && !isDead, body.getPosition(), false, bodyFrame.timeDelta);
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

		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChanged = nextMoveState != moveState;
		switch(nextMoveState) {
			case FLY:
				horizGoState = getNextAxisGoState(true, horizGoState);
				vertGoState = getNextAxisGoState(false, vertGoState);
				if(isHorizontalOnly)
					body.getSpine().applyAxisMoves(horizGoState, null);
				else
					body.getSpine().applyAxisMoves(horizGoState, vertGoState);
				break;
			case DEAD:
				parent.getAgency().createAgent(VanishPoof.makeAP(body.getPosition(), true));
				parent.getAgency().createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
				parent.getAgency().removeAgent(parent);
				parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				break;
		}

		moveStateTimer = isMoveStateChanged ? 0f : moveStateTimer+delta;
		moveState = nextMoveState;
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

	private MoveState getNextMoveState() {
		if(isDead || moveState == MoveState.DEAD)
			return MoveState.DEAD;
		else
			return MoveState.FLY;
	}

	// assume any amount of damage kills, for now...
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}
}
