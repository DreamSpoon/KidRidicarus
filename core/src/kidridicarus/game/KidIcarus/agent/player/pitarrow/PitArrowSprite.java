package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.KidIcarusGfx;

class PitArrowSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(4);
	private static final float SPRITE_OFFSET_RIGHT = UInfo.P2M(4);
	private static final float SPRITE_OFFSET_UP = UInfo.P2M(-4);

	PitArrowSprite(TextureAtlas atlas, SpriteFrameInput frameInput) {
		setRegion(atlas.findRegion(KidIcarusGfx.Player.PitArrow.ARROW));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(frameInput);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(preFrameInput(frameInput))
			postFrameInput((PitArrowSpriteFrameInput) frameInput);
	}

	private void postFrameInput(PitArrowSpriteFrameInput frameInput) {
		boolean arrowFlipX = frameInput.arrowDir == Direction4.LEFT;
		boolean arrowFlipY = frameInput.arrowDir == Direction4.DOWN;
		flip(arrowFlipX ^ isFlipX(), arrowFlipY ^ isFlipY());

		// rotate about the center of the sprite
		setOrigin(getWidth()/2f, getHeight()/2f);
		setRotation(frameInput.arrowDir.isVertical() ? 90f : 0f);

		Vector2 offset = getOffset(frameInput.arrowDir);
		setPosition(frameInput.position.x - getWidth()/2f + offset.x,
				frameInput.position.y - getHeight()/2f + offset.y);
	}

	private Vector2 getOffset(Direction4 arrowDir) {
		switch(arrowDir) {
			case RIGHT:
				return new Vector2(SPRITE_OFFSET_RIGHT, 0f);
			case LEFT:
				return new Vector2(-SPRITE_OFFSET_RIGHT, 0f);
			case UP:
				return new Vector2(0f, SPRITE_OFFSET_UP);
			case DOWN:
				return new Vector2(0f, -SPRITE_OFFSET_UP);
			default:
				return new Vector2(0f, 0f);
		}
	}
}
