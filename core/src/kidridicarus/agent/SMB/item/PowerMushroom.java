package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.info.GameInfo;
import kidridicarus.info.SMBInfo.PowerupType;
import kidridicarus.tools.EncapTexAtlas;

public class PowerMushroom extends BaseMushroom {
	public PowerMushroom(Agency agency, AgentDef adef) {
		super(agency, adef);
	}

	@Override
	public void use(Agent agent) {
		if(isSprouting)
			return;

		if(agent instanceof Mario) {
			((Mario) agent).applyPowerup(PowerupType.MUSHROOM);
			agency.disposeAgent(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(EncapTexAtlas atlas) {
		return atlas.findRegion(GameInfo.TEXATLAS_MUSHROOM);
	}
}
