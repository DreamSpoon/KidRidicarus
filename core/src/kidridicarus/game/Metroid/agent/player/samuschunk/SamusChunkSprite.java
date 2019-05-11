package kidridicarus.game.Metroid.agent.player.samuschunk;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction8;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.MetroidGfx;

class SamusChunkSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 1/30f;

	private Animation<TextureRegion> anim;
	private float animTimer;

	SamusChunkSprite(TextureAtlas atlas, Vector2 position, Direction8 startDir) {
		animTimer = 0f;
		switch(startDir) {
			case DOWN_RIGHT:
				anim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.BOT_RIGHT), PlayMode.LOOP);
				break;
			case DOWN_LEFT:
				anim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.BOT_LEFT), PlayMode.LOOP);
				break;
			case RIGHT:
				anim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.MID_RIGHT), PlayMode.LOOP);
				break;
			case LEFT:
				anim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.MID_LEFT), PlayMode.LOOP);
				break;
			case UP_RIGHT:
				anim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.TOP_RIGHT), PlayMode.LOOP);
				break;
			case UP_LEFT:
				anim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.Dead.TOP_LEFT), PlayMode.LOOP);
				break;
			default:
				throw new IllegalArgumentException("Cannot set sprite region for SamusChunk startDir = " + startDir);
		}
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		animTimer += frameInput.frameTime.timeDelta;
		setRegion(anim.getKeyFrame(animTimer));
		postFrameInput(frameInput);
	}
}
