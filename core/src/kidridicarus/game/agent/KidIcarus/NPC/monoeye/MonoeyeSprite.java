package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.BasicAgentSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class MonoeyeSprite extends BasicAgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public MonoeyeSprite(TextureAtlas atlas, Vector2 position) {
		super(atlas.findRegion(KidIcarusGfx.NPC.MONOEYE), SPRITE_WIDTH, SPRITE_HEIGHT, position);
	}
}
