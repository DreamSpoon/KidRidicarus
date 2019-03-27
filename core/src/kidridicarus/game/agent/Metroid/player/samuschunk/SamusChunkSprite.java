package kidridicarus.game.agent.Metroid.player.samuschunk;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction8;
import kidridicarus.game.info.MetroidGfx;

public class SamusChunkSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 1/30f;

	private Animation<TextureRegion> chunkAnim;
	private float stateTimer;

	public SamusChunkSprite(TextureAtlas atlas, Vector2 position, Direction8 startDir) {
		stateTimer = 0f;
		switch(startDir) {
			case DOWN_RIGHT:
				chunkAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.BOT_RIGHT), PlayMode.LOOP);
				break;
			case DOWN_LEFT:
				chunkAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.BOT_LEFT), PlayMode.LOOP);
				break;
			case RIGHT:
				chunkAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.MID_RIGHT), PlayMode.LOOP);
				break;
			case LEFT:
				chunkAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.MID_LEFT), PlayMode.LOOP);
				break;
			case UP_RIGHT:
				chunkAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.TOP_RIGHT), PlayMode.LOOP);
				break;
			case UP_LEFT:
				chunkAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.TOP_LEFT), PlayMode.LOOP);
				break;
			default:
				throw new IllegalArgumentException("Cannot set sprite region for SamusChunk startDir = " + startDir);
		}

		setRegion(chunkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position, float delta) {
		setRegion(chunkAnim.getKeyFrame(stateTimer));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer += delta;
	}
}
