package kidridicarus.game.SMB1.agent.item.staticcoin;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_Pow;

public class StaticCoinBrain {
	private StaticCoin parent;
	private StaticCoinBody body;
	private boolean isUsed;
	private boolean despawnMe;

	public StaticCoinBrain(StaticCoin parent, StaticCoinBody body) {
		this.parent = parent;
		this.body = body;
		isUsed = false;
		despawnMe = false;
	}

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
		if(!isUsed && (!cFrameInput.isKeepAlive || cFrameInput.isDespawn))
			despawnMe = true;
	}

	public SpriteFrameInput processFrame() {
		if(isUsed) {
			parent.getAgency().getEar().playSound(SMB1_Audio.Sound.COIN);
			parent.getAgency().removeAgent(parent);
			return null;
		}
		else if(despawnMe)
			return null;
		// override the usual delta time with an absolute time from getGlobalTimer
		return new SpriteFrameInput(true, parent.getAgency().getGlobalTimer(), false, false, 0f, body.getPosition());
	}
}
