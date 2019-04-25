package kidridicarus.game.SMB1.agent.item.staticcoin;

import kidridicarus.common.agent.halfactor.HalfActorBrain;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_Pow;

public class StaticCoinBrain extends HalfActorBrain {
	private StaticCoin parent;
	private StaticCoinBody body;
	private boolean isUsed;

	public StaticCoinBrain(StaticCoin parent, StaticCoinBody body) {
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
		if(taker.onTakePowerup(new SMB1_Pow.CoinPow()))
			isUsed = true;
	}

	@Override
	public AnimSpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parent.getAgency().getEar().playSound(SMB1_Audio.Sound.COIN);
			parent.getAgency().removeAgent(parent);
		}
		// override the usual delta time with an absolute time with getGlobalTimer
		return new AnimSpriteFrameInput(true, body.getPosition(), false, parent.getAgency().getGlobalTimer());
	}
}
