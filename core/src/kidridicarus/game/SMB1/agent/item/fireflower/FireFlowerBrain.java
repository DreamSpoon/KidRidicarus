package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.SMB1.agentsprite.SproutSpriteFrameInput;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_Pow;

class FireFlowerBrain {
	private static final float SPROUT_TIME = 1f;
	private static final float SPROUT_OFFSET = UInfo.P2M(-13f);

	private enum MoveState { SPROUT, WALK, END }

	private AgentHooks parentHooks;
	private FireFlowerBody body;
	private float moveStateTimer;
	private MoveState moveState;
	private Vector2 initSpawnPosition;
	private PowerupTakeAgent powerupTaker;
	private boolean despawnMe;

	FireFlowerBrain(AgentHooks parentHooks, FireFlowerBody body, Vector2 initSpawnPosition) {
		this.parentHooks = parentHooks;
		this.body = body;
		this.initSpawnPosition = initSpawnPosition;
		moveStateTimer = 0f;
		moveState = MoveState.SPROUT;
		powerupTaker = null;
		despawnMe = false;
	}

	Vector2 getSproutStartPos() {
		return initSpawnPosition.cpy().add(0f, SPROUT_OFFSET);
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if not finished sprouting or if used
		if(moveState == MoveState.SPROUT || powerupTaker != null)
			return;
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new SMB1_Pow.FireFlowerPow()))
			powerupTaker = taker;
		// if not touching keep alive box or if touching despawn then despawn
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn)
			despawnMe = true;
	}

	SproutSpriteFrameInput processFrame(FrameTime frameTime) {
		Vector2 spritePos = new Vector2();
		boolean finishSprout = false;
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case SPROUT:
				spritePos.set(initSpawnPosition.cpy().add(0f,
						SPROUT_OFFSET * (SPROUT_TIME - moveStateTimer) / SPROUT_TIME));
				break;
			case WALK:
				if(isMoveStateChange) {
					finishSprout = true;
					body.finishSprout(initSpawnPosition);
				}
				spritePos.set(body.getPosition());
				break;
			case END:
				if(powerupTaker != null) {
					parentHooks.getEar().playSound(SMB1_Audio.Sound.POWERUP_USE);
					parentHooks.createAgent(FloatingPoints.makeAP(1000, true, body.getPosition(),
							(Agent) powerupTaker));
				}
				parentHooks.removeThisAgent();
				return null;
		}
		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		return new SproutSpriteFrameInput(spritePos, frameTime, finishSprout);
	}

	private MoveState getNextMoveState() {
		if(despawnMe || powerupTaker != null)
			return MoveState.END;
		else if(moveState == MoveState.WALK || (moveState == MoveState.SPROUT && moveStateTimer > SPROUT_TIME))
			return MoveState.WALK;
		else
			return MoveState.SPROUT;
	}
}
