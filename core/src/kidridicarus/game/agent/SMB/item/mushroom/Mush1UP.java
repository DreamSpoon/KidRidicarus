package kidridicarus.game.agent.SMB.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.game.info.SMBAnim;
import kidridicarus.game.info.PowerupInfo.PowType;

public class Mush1UP extends BaseMushroom {
	public Mush1UP(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	@Override
	public void use(Agent agent) {
		if(isSprouting)
			return;

		if(agent instanceof PowerupTakeAgent) {
			((PowerupTakeAgent) agent).onTakePowerup(PowType.MUSH1UP);
			agency.disposeAgent(this);
		}
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(SMBAnim.Item.MUSH1UP);
	}
}
