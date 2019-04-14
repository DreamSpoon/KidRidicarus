package kidridicarus.game.agent.KidIcarus.item.angelheart;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.staticpowerup.StaticPowerupSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.KidIcarus.item.angelheart.AngelHeart.AngelHeartSize;
import kidridicarus.game.info.KidIcarusGfx;

public class AngelHeartSprite extends StaticPowerupSprite {
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float MED_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float MED_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(16);

	public AngelHeartSprite(TextureAtlas atlas, Vector2 position, AngelHeartSize heartSize) {
		super(getSprAnim(atlas, heartSize), getSprWidth(heartSize), getSprHeight(heartSize), position);
	}

	private static Animation<TextureRegion> getSprAnim(TextureAtlas atlas, AngelHeartSize heartSize) {
		switch(heartSize) {
			case FULL:
				return new Animation<TextureRegion>(1f, atlas.findRegions(KidIcarusGfx.Item.HEART10), PlayMode.LOOP);
			case HALF:
				return new Animation<TextureRegion>(1f, atlas.findRegions(KidIcarusGfx.Item.HEART5), PlayMode.LOOP);
			case SMALL:
			default:
				return new Animation<TextureRegion>(1f, atlas.findRegions(KidIcarusGfx.Item.HEART1), PlayMode.LOOP);
		}
	}

	private static float getSprWidth(AngelHeartSize heartSize) {
		switch(heartSize) {
			case FULL:
				return BIG_SPRITE_WIDTH;
			case HALF:
				return MED_SPRITE_WIDTH;
			case SMALL:
			default:
				return SML_SPRITE_WIDTH;
		}
	}

	private static float getSprHeight(AngelHeartSize heartSize) {
		switch(heartSize) {
			case FULL:
				return BIG_SPRITE_HEIGHT;
			case HALF:
				return MED_SPRITE_HEIGHT;
			case SMALL:
			default:
				return SML_SPRITE_HEIGHT;
		}
	}
}
