package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.KidIcarusAudio;
import kidridicarus.game.info.KidIcarusPow;

public class ChaliceHealth extends StaticPowerup implements DisposableAgent {
	public ChaliceHealth(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new ChaliceHealthBody(this, agency.getWorld(), AP_Tool.getCenter(agentProps));
		sprite = new ChaliceHealthSprite(agency.getAtlas(), AP_Tool.getCenter(agentProps));
	}

	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed)
			agency.getEar().playSound(KidIcarusAudio.Sound.General.HEART_PICKUP);
		return true;
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new KidIcarusPow.ChaliceHealthPow();
	}
}
