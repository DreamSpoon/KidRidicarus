package kidridicarus.game.SMB1.agent.item.staticcoin;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.game.SMB1.SMB1_Audio;
import kidridicarus.game.SMB1.SMB1_Pow;

class StaticCoinBrain {
	private AgentHooks parentHooks;
	private StaticCoinBody body;
	private boolean isUsed;
	private boolean despawnMe;

	StaticCoinBrain(AgentHooks parentHooks, StaticCoinBody body) {
		this.parentHooks = parentHooks;
		this.body = body;
		isUsed = false;
		despawnMe = false;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if not used
		if(isUsed)
			return;
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new SMB1_Pow.CoinPow()))
			isUsed = true;
		if(!isUsed && (!cFrameInput.isKeepAlive || cFrameInput.isDespawn))
			despawnMe = true;
	}

	SpriteFrameInput processFrame(FrameTime frameTime) {
		if(isUsed) {
			parentHooks.getEar().playSound(SMB1_Audio.Sound.COIN);
			parentHooks.removeThisAgent();
			return null;
		}
		else if(despawnMe)
			return null;
		// override the usual delta time with an absolute time from getGlobalTimer
		return new SpriteFrameInput(frameTime, false, false, 0f, body.getPosition());
	}
}
