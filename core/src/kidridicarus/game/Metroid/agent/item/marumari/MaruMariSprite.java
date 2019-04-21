package kidridicarus.game.Metroid.agent.item.marumari;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.BasicAgentSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.MetroidGfx;

public class MaruMariSprite extends BasicAgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.033f;

	public MaruMariSprite(TextureAtlas atlas, Vector2 position) {
		super(new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(MetroidGfx.Item.MARUMARI),
				PlayMode.LOOP), SPRITE_WIDTH, SPRITE_HEIGHT, position);
	}
}
