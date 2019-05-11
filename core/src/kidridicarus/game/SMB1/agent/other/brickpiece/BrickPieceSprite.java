package kidridicarus.game.SMB1.agent.other.brickpiece;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.SMB1_Gfx;

class BrickPieceSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 0.2f;

	private Animation<TextureRegion> anim;
	private float animTimer;

	BrickPieceSprite(TextureAtlas atlas, Vector2 position, int startFrame) {
		anim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.General.BRICKPIECE), PlayMode.LOOP);
		animTimer = (float) startFrame * ANIM_SPEED;
		setRegion(anim.getKeyFrame(animTimer));
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
