package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.SMB1_Gfx;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.info.SMB1_Pow;

public class Up1Mushroom extends BaseMushroom {
	public Up1Mushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	@Override
	protected TextureRegion getMushroomTexture(TextureAtlas atlas) {
		return atlas.findRegion(SMB1_Gfx.Item.UP1_MUSHROOM);
	}

	@Override
	protected Powerup getPowerupPow() {
		return new SMB1_Pow.Mush1UpPow();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_UP1_MUSHROOM, position);
	}
}
