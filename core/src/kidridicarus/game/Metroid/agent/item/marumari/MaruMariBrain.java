package kidridicarus.game.Metroid.agent.item.marumari;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidPow;

public class MaruMariBrain {
	private MaruMari parent;
	private MaruMariBody body;
	private boolean isUsed;

	public MaruMariBrain(MaruMari parent, MaruMariBody body) {
		this.parent = parent;
		this.body = body;
		isUsed = false;
	}

	public void processContactFrame(BrainContactFrameInput cFrameInput) {
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

	public SpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parent.getAgency().getEar().startSinglePlayMusic(MetroidAudio.Music.GET_ITEM);
			parent.getAgency().removeAgent(parent);
			return null;
		}
		return SprFrameTool.placeAnim(body.getPosition(), delta);
	}
}
