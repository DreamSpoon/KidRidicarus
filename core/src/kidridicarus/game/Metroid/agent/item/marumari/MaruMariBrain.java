package kidridicarus.game.Metroid.agent.item.marumari;

import kidridicarus.common.agent.halfactor.HalfActorBrain;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidPow;

public class MaruMariBrain extends HalfActorBrain {
	private MaruMari parent;
	private MaruMariBody body;
	private boolean isUsed;

	public MaruMariBrain(MaruMari parent, MaruMariBody body) {
		this.parent = parent;
		this.body = body;
		isUsed = false;
	}

	@Override
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

	@Override
	public AnimSpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parent.getAgency().getEar().startSinglePlayMusic(MetroidAudio.Music.GET_ITEM);
			parent.getAgency().removeAgent(parent);
		}
		return new AnimSpriteFrameInput(true, body.getPosition(), false, delta);
	}
}
