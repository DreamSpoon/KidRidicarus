package kidridicarus.game.agent.SMB1.item.staticcoin;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.powerup.SMB1_Pow;

public class StaticCoin extends StaticPowerup implements DisposableAgent {
	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new StaticCoinBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		sprite = new StaticCoinSprite(agency.getAtlas(), Agent.getStartPoint(properties));
	}

	// always returns false, since this method completely overrides the original, due to global timer sprite update
	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed) {
			agency.getEar().playSound(SMB1_Audio.Sound.COIN);
			agency.removeAgent(this);
			return false;
		}

		sprite.update(agency.getGlobalTimer(), true, body.getPosition());
		return false;
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new SMB1_Pow.CoinPow();
	}
}
