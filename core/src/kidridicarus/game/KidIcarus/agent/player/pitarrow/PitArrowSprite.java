package kidridicarus.game.KidIcarus.agent.player.pitarrow;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.info.KidIcarusGfx;

public class PitArrowSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(4);
	private static final float SPRITE_OFFSET_RIGHT = UInfo.P2M(4);
	private static final float SPRITE_OFFSET_UP = UInfo.P2M(-4);
	private static final float ORIGIN_OFFSET_UP = UInfo.P2M(5.5f);
	private Direction4 arrowDir;

	public PitArrowSprite(TextureAtlas atlas, Vector2 position, Direction4 arrowDir) {
		super(true);
		this.arrowDir = arrowDir;
		setRegion(atlas.findRegion(KidIcarusGfx.Player.PitArrow.ARROW));
		if((arrowDir == Direction4.RIGHT && isFlipX()) || (arrowDir == Direction4.LEFT && !isFlipX()))
			flip(true,  false);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		Vector2 pos = getArrowPosition(position);
		setPosition(pos.x-getWidth()/2f, pos.y-getHeight()/2f);
		if(arrowDir == Direction4.UP) {
			setOrigin(0f, ORIGIN_OFFSET_UP);
			setRotation(90f);
		}
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		applyFrameInput(new SpriteFrameInput(frameInput.visible, getArrowPosition(frameInput.position),
				frameInput.flipX));
	}

	private Vector2 getArrowPosition(Vector2 position) {
		switch(arrowDir) {
			case RIGHT:
			default:
				return new Vector2(position.x + SPRITE_OFFSET_RIGHT, position.y);
			case LEFT:
				return new Vector2(position.x - SPRITE_OFFSET_RIGHT, position.y);
			case UP:
				return new Vector2(position.x, position.y + SPRITE_OFFSET_UP);
		}
	}
}
