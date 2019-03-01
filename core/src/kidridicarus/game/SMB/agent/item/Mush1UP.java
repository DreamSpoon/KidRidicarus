package kidridicarus.game.SMB.agent.item;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentProperties;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.info.SMBAnim;
import kidridicarus.game.info.PowerupInfo.PowType;

public class Mush1UP extends BaseMushroom {
	public Mush1UP(Agency agency, AgentProperties properties) {
		super(agency, properties);
	}

	@Override
	public void use(Agent agent) {
		if(isSprouting)
			return;

		if(agent instanceof Mario) {
			((Mario) agent).applyPowerup(PowType.MUSH1UP);
			agency.disposeAgent(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(SMBAnim.Item.MUSH1UP);
	}
}
