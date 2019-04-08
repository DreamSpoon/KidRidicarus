package kidridicarus.game.agent.SMB1.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.info.SMB1_Gfx;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.SMB1_Pow;

public class MagicMushroom extends BaseMushroom {
	public MagicMushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	@Override
	protected TextureRegion getMushroomTextureRegion(TextureAtlas atlas) {
		return atlas.findRegion(SMB1_Gfx.Item.MAGIC_MUSHROOM);
	}

	@Override
	protected Powerup getMushroomPowerup() {
		return new SMB1_Pow.MushroomPow();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return Agent.createPointAP(SMB1_KV.AgentClassAlias.VAL_MAGIC_MUSHROOM, position);
	}
}
