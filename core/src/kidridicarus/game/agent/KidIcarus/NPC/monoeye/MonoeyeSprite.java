package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class MonoeyeSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public MonoeyeSprite(TextureAtlas atlas, Vector2 position) {
		setRegion(atlas.findRegion(KidIcarusGfx.NPC.MONOEYE));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position, boolean isFacingRight) {
		if((isFacingRight && isFlipX()) || (!isFacingRight && !isFlipX()))
			flip(true,  false);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
