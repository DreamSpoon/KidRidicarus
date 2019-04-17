package kidridicarus.game.agent.SMB1.item.mushroom;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.SMB1_Gfx;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.SMB1_Pow;

public class Up1Mushroom extends BaseMushroom {
	private static final float ANIM_SPEED = 1f;

	public Up1Mushroom(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	@Override
	protected Animation<TextureRegion> getMushroomAnim(TextureAtlas atlas) {
		return new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMB1_Gfx.Item.UP1_MUSHROOM), PlayMode.LOOP);
	}

	@Override
	protected Powerup getPowerupPow() {
		return new SMB1_Pow.Mush1UpPow();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_UP1_MUSHROOM, position);
	}
}
