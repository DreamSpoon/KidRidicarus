package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.info.KidIcarusGfx;

public class PitArrowSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(4);
	private static final float SPRITE_OFFSET_RIGHT = UInfo.P2M(4);
	private static final float SPRITE_OFFSET_UP = UInfo.P2M(-4);
	private static final float ORIGIN_OFFSET_UP = UInfo.P2M(5.5f);

	public PitArrowSprite(TextureAtlas atlas, Vector2 position, Direction4 arrowDir) {
		setRegion(atlas.findRegion(KidIcarusGfx.Player.PitArrow.ARROW));
		if((arrowDir == Direction4.RIGHT && isFlipX()) || (arrowDir == Direction4.LEFT && !isFlipX()))
			flip(true,  false);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		doSetPosition(position, arrowDir);
		if(arrowDir== Direction4.UP) {
			setOrigin(0f, ORIGIN_OFFSET_UP);
			setRotation(90f);
		}
	}

	public void update(Vector2 position, Direction4 arrowDir) {
		doSetPosition(position, arrowDir);
	}

	private void doSetPosition(Vector2 position, Direction4 arrowDir) {
		switch(arrowDir) {
			case RIGHT:
				setPosition(position.x - getWidth()/2f + SPRITE_OFFSET_RIGHT, position.y - getHeight()/2f);
				break;
			case LEFT:
				setPosition(position.x - getWidth()/2f - SPRITE_OFFSET_RIGHT, position.y - getHeight()/2f);
				break;
			case UP:
				setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f + SPRITE_OFFSET_UP);
				break;
			default:
		}
	}
}
