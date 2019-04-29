package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agent.halfactor.HalfActorBrain;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusPow;

public class ChaliceHealthBrain extends HalfActorBrain {
	private ChaliceHealth parent;
	private ChaliceHealthBody body;
	private boolean isUsed;

	public ChaliceHealthBrain(ChaliceHealth parent, ChaliceHealthBody body) {
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
		if(taker.onTakePowerup(new KidIcarusPow.ChaliceHealthPow()))
			isUsed = true;
	}

	@Override
	public SpriteFrameInput processFrame(float delta) {
		if(isUsed) {
			parent.getAgency().getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
			parent.getAgency().removeAgent(parent);
		}
		return new SpriteFrameInput(body.getPosition());
	}
}
