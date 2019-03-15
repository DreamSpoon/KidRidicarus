package kidridicarus.game.agent.SMB.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.game.info.SMBAnim;
import kidridicarus.game.info.PowerupInfo.PowType;

public class Mush1UP extends BaseMushroom {
	public Mush1UP(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(SMBAnim.Item.MUSH1UP);
	}

	@Override
	protected PowType getMushroomPowerup() {
		return PowType.MUSH1UP;
	}
}
