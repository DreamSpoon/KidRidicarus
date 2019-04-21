package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.BasicAgentSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class ChaliceHealthSprite extends BasicAgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public ChaliceHealthSprite(TextureAtlas atlas, Vector2 position) {
		super(new Animation<TextureRegion>(1f, atlas.findRegions(KidIcarusGfx.Item.CHALICE), PlayMode.LOOP),
				SPRITE_WIDTH, SPRITE_HEIGHT, position);
	}
}
