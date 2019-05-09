package kidridicarus.game.Metroid.agent.item.energy;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidPow;

public class EnergyBrain {
	private static final float LIVE_TIME = 6.35f;

	private Energy parent;
	private EnergyBody body;
	private boolean isUsed;
	private float moveStateTimer;

	public EnergyBrain(Energy parent, EnergyBody body) {
		this.parent = parent;
		this.body = body;
		isUsed = false;
		moveStateTimer = 0f;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
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

	public SpriteFrameInput processFrame(FrameTime frameTime) {
		if(isUsed) {
			parent.getAgency().getEar().playSound(MetroidAudio.Sound.ENERGY_PICKUP);
			parent.getAgency().removeAgent(parent);
			return null;
		}
		moveStateTimer += frameTime.timeDelta;
		if(moveStateTimer > LIVE_TIME) {
			parent.getAgency().removeAgent(parent);
			return null;
		}
		return SprFrameTool.placeAnim(body.getPosition(), frameTime);
	}
}
