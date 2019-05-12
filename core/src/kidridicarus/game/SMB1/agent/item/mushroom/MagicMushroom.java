package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.SMB1_Gfx;
import kidridicarus.game.SMB1.SMB1_KV;
import kidridicarus.game.SMB1.SMB1_Pow;

public class MagicMushroom extends BaseMushroom {
	public MagicMushroom(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
	}

	@Override
	protected TextureRegion getMushroomTexture(TextureAtlas atlas) {
		return atlas.findRegion(SMB1_Gfx.Item.MAGIC_MUSHROOM);
	}

	@Override
	protected Powerup getPowerupPow() {
		return new SMB1_Pow.MushroomPow();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_MAGIC_MUSHROOM, position);
	}
}
