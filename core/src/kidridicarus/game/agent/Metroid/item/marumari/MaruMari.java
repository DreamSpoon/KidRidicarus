package kidridicarus.game.agent.Metroid.item.marumari;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.powerup.MetroidPow;

public class MaruMari extends StaticPowerup implements DisposableAgent {
	public MaruMari(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new MaruMariBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		sprite = new MaruMariSprite(agency.getAtlas(), Agent.getStartPoint(agentProps));
	}

	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(isPowUsed)
			agency.getEar().startSinglePlayMusic(MetroidAudio.Music.GET_ITEM);
		return true;
	}

	@Override
	protected Powerup getStaticPowerupPow() {
		return new MetroidPow.MaruMariPow();
	}
}
