package kidridicarus.game.Metroid.agent.item.energy;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.Metroid.MetroidAudio;
import kidridicarus.game.Metroid.MetroidPow;

class EnergyBrain {
	private static final float LIVE_TIME = 6.35f;

	private AgentHooks parentHooks;
	private EnergyBody body;
	private boolean isUsed;
	private float moveStateTimer;

	EnergyBrain(AgentHooks parentHooks, EnergyBody body) {
		this.parentHooks = parentHooks;
		this.body = body;
		isUsed = false;
		moveStateTimer = 0f;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if not used
		if(isUsed)
			return;
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new MetroidPow.EnergyPow()))
			isUsed = true;
	}

	SpriteFrameInput processFrame(FrameTime frameTime) {
		if(isUsed) {
			parentHooks.getEar().playSound(MetroidAudio.Sound.ENERGY_PICKUP);
			parentHooks.removeThisAgent();
			return null;
		}
		moveStateTimer += frameTime.timeDelta;
		if(moveStateTimer > LIVE_TIME) {
			parentHooks.removeThisAgent();
			return null;
		}
		return SprFrameTool.placeAnim(body.getPosition(), frameTime);
	}
}
