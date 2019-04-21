package kidridicarus.game.Metroid.agent.item.marumari;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.MetroidAudio;
import kidridicarus.game.info.MetroidPow;

public class MaruMari extends StaticPowerup implements DisposableAgent {
	public MaruMari(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new MaruMariBody(this, agency.getWorld(), AP_Tool.getCenter(agentProps));
		sprite = new MaruMariSprite(agency.getAtlas(), AP_Tool.getCenter(agentProps));
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
