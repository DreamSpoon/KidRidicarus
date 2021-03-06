package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.Metroid.MetroidGfx;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShotBrain.MoveState;

class SamusShotSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 1f/60f;

	private Animation<TextureRegion> liveAnim;
	private Animation<TextureRegion> explodeAnim;
	private float animTimer;
	private MoveState parentPrevMoveState;

	SamusShotSprite(TextureAtlas atlas, Vector2 position) {
		liveAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.Player.SamusShot.SHOT), PlayMode.LOOP);
		explodeAnim = new Animation<TextureRegion>(ANIM_SPEED,
						atlas.findRegions(MetroidGfx.Player.SamusShot.SHOT_EXP), PlayMode.LOOP);
		animTimer = 0f;
		parentPrevMoveState = null;
		setRegion(liveAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		MoveState parentMoveState = ((SamusShotSpriteFrameInput) frameInput).moveState;
		animTimer = parentMoveState != parentPrevMoveState ? 0f : animTimer+frameInput.frameTime.timeDelta;
		parentPrevMoveState = parentMoveState;
		switch(parentMoveState) {
			case LIVE:
			case DEAD:
				setRegion(liveAnim.getKeyFrame(animTimer));
				break;
			case EXPLODE:
				setRegion(explodeAnim.getKeyFrame(animTimer));
				break;
		}
		postFrameInput(frameInput);
	}
}
