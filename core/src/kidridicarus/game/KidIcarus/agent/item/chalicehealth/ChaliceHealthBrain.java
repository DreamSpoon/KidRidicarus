package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusPow;

public class ChaliceHealthBrain {
	private ChaliceHealth parent;
	private ChaliceHealthBody body;
	private boolean isUsed;
	private boolean despawnMe;

	public ChaliceHealthBrain(ChaliceHealth parent, ChaliceHealthBody body) {
		this.parent = parent;
		this.body = body;
		isUsed = false;
		despawnMe = false;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
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

	public SpriteFrameInput processFrame() {
		if(isUsed) {
			parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			parent.getAgency().removeAgent(parent);
			return null;
		}
		else if(despawnMe) {
			parent.getAgency().removeAgent(parent);
			return null;
		}
		return SprFrameTool.place(body.getPosition());
	}
}
