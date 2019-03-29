package kidridicarus.game.agent.KidIcarus.item.heart1;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class Heart1Sprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);

	public Heart1Sprite(TextureAtlas atlas, Vector2 position) {
		setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART1));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
