package kidridicarus.game.Metroid.agent.item.marumari;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidPow;

class MaruMariBrain {
	private AgentHooks parentHooks;
	private MaruMariBody body;
	private boolean isUsed;

	MaruMariBrain(AgentHooks parentHooks, MaruMariBody body) {
		this.parentHooks = parentHooks;
		this.body = body;
		isUsed = false;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if not used
		if(isUsed)
			return;
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new MetroidPow.MaruMariPow()))
			isUsed = true;
	}

	SpriteFrameInput processFrame(FrameTime frameTime) {
		if(isUsed) {
			parentHooks.getEar().startSinglePlayMusic(MetroidAudio.Music.GET_ITEM);
			parentHooks.removeThisAgent();
			return null;
		}
		return SprFrameTool.placeAnim(body.getPosition(), frameTime);
	}
}
