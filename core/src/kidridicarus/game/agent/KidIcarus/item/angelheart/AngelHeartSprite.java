package kidridicarus.game.agent.KidIcarus.item.angelheart;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.KidIcarus.item.angelheart.AngelHeart.AngelHeartSize;
import kidridicarus.game.info.KidIcarusGfx;

public class AngelHeartSprite extends Sprite {
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float MED_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float MED_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(16);

	public AngelHeartSprite(TextureAtlas atlas, Vector2 position, AngelHeartSize heartSize) {
		switch(heartSize) {
			case SMALL:
				setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART1));
				setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
				break;
			case HALF:
				setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART5));
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				break;
			case FULL:
				setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART10));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				break;
		}
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
