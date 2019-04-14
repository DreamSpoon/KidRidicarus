package kidridicarus.game.agent.Metroid.item.energy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.staticpowerup.StaticPowerupSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.MetroidGfx;

public class EnergySprite extends StaticPowerupSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 1/30f;

	public EnergySprite(TextureAtlas atlas, Vector2 position) {
		super(new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(MetroidGfx.Item.ENERGY), PlayMode.LOOP),
				SPRITE_WIDTH, SPRITE_HEIGHT, position);
	}
}
