package kidridicarus.game.KidIcarus.agent.NPC.specknose;

import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.agent.other.vanishpoof.VanishPoof;
import kidridicarus.game.KidIcarus.agentspine.FlyBallSpine.AxisGoState;
import kidridicarus.game.info.KidIcarusAudio;

public class SpecknoseBrain {
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

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
		// push damage to contact damage agents
		for(ContactDmgTakeAgent agent : ((ContactDmgBrainContactFrameInput) cFrameInput).contactDmgTakeAgents)
			agent.onTakeDamage(parent, GIVE_DAMAGE, body.getPosition());
		// check for despawn
		if(!isDead && (!cFrameInput.isKeepAlive || cFrameInput.isDespawn))
			despawnMe = true;
	}

	public SpriteFrameInput processFrame(FrameTime frameTime) {
		// if despawning then dispose and exit
		if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return null;
		}
		MoveState nextMoveState = getNextMoveState();
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
				parent.getAgency().removeAgent(parent);
				parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.SMALL_POOF);
				parent.getAgency().createAgent(VanishPoof.makeAP(body.getPosition(), true));
				parent.getAgency().createAgent(AngelHeart.makeAP(body.getPosition(), DROP_HEART_COUNT));
				return null;
		}
		moveStateTimer = nextMoveState != moveState ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		return SprFrameTool.placeAnim(body.getPosition(), frameTime);
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

	public boolean onTakeDamage(Agent agent) {
		// if dead already or the damage is from the same team then return no damage taken
		if(isDead || !(agent instanceof PlayerAgent))
			return false;

		isDead = true;
		return true;
	}
}
