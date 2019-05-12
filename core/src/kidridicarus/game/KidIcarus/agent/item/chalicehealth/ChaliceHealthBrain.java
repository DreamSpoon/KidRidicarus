package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.KidIcarus.KidIcarusAudio;
import kidridicarus.game.KidIcarus.KidIcarusPow;

class ChaliceHealthBrain {
	private AgentHooks parentHooks;
	private ChaliceHealthBody body;
	private boolean isUsed;
	private boolean despawnMe;

	ChaliceHealthBrain(AgentHooks parentHooks, ChaliceHealthBody body) {
		this.parentHooks = parentHooks;
		this.body = body;
		isUsed = false;
		despawnMe = false;
	}

	void processContactFrame(BrainContactFrameInput cFrameInput) {
		// exit if used
		if(isUsed)
			return;
		if(!cFrameInput.isKeepAlive || cFrameInput.isDespawn) {
			despawnMe = true;
			return;
		}
		// if any agents touching this powerup are able to take it, then push it to them
		PowerupTakeAgent taker = ((PowerupBrainContactFrameInput) cFrameInput).powerupTaker;
		if(taker == null)
			return;
		if(taker.onTakePowerup(new KidIcarusPow.ChaliceHealthPow()))
			isUsed = true;
	}

	SpriteFrameInput processFrame() {
		if(isUsed) {
			parentHooks.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			parentHooks.removeThisAgent();
			return null;
		}
		else if(despawnMe) {
			parentHooks.removeThisAgent();
			return null;
		}
		return SprFrameTool.place(body.getPosition());
	}
}
