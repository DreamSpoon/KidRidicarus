package kidridicarus.game.agent.SMB1.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.Agency;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.SMB1_Gfx;
import kidridicarus.game.powerup.SMB1_Pow;

public class Mush1UP extends BaseMushroom {
	public Mush1UP(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(SMB1_Gfx.Item.MUSH1UP);
	}

	@Override
	protected Powerup getMushroomPowerup() {
		return new SMB1_Pow.Mush1UpPow();
	}
}