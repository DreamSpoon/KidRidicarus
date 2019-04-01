package kidridicarus.game.agent.KidIcarus.player.pitarrow;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.info.KidIcarusGfx;

public class PitArrowSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(4);

	public PitArrowSprite(TextureAtlas atlas, Vector2 position, Direction4 arrowDir) {
		setRegion(atlas.findRegion(KidIcarusGfx.Player.PitArrow.ARROW));
		if((arrowDir == Direction4.RIGHT && isFlipX()) || (arrowDir == Direction4.LEFT && !isFlipX()))
			flip(true,  false);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position) {
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
