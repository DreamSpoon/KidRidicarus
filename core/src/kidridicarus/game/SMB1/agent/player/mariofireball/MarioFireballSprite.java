package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireballBrain.MoveState;
import kidridicarus.game.info.SMB1_Gfx;

public class MarioFireballSprite extends AgentSprite {
	private static final float BALL_WIDTH = UInfo.P2M(8);
	private static final float BALL_HEIGHT = UInfo.P2M(8);
	private static final float EXPLODE_WIDTH = UInfo.P2M(16);
	private static final float EXPLODE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED_FLY = 1/15f;
	private static final float ANIM_SPEED_EXP = 1/30f;

	private Animation<TextureRegion> ballAnim;
	private Animation<TextureRegion> explodeAnim;
	private float animTimer;
	private MoveState prevParentMoveState;

	public MarioFireballSprite(TextureAtlas atlas, Vector2 position) {
		ballAnim = new Animation<TextureRegion>(ANIM_SPEED_FLY,
				atlas.findRegions(SMB1_Gfx.Player.MarioFireball.FIREBALL), PlayMode.LOOP);
		explodeAnim = new Animation<TextureRegion>(ANIM_SPEED_EXP,
				atlas.findRegions(SMB1_Gfx.Player.MarioFireball.FIREBALL_EXP), PlayMode.NORMAL);
		animTimer = 0f;
		prevParentMoveState = null;
		setRegion(ballAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), BALL_WIDTH, BALL_HEIGHT);
		applyFrameInput(new SpriteFrameInput(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		MarioFireballSpriteFrame myFrameInput = (MarioFireballSpriteFrame) frameInput;
		boolean isMoveStateChange = myFrameInput.moveState != prevParentMoveState;
		animTimer = isMoveStateChange ? 0f : animTimer+myFrameInput.timeDelta;
		switch(myFrameInput.moveState) {
			case FLY:
				setRegion(ballAnim.getKeyFrame(animTimer));
				break;
			case EXPLODE:
				// change the size of the sprite when it changes to an explosion
				if(isMoveStateChange)
					setBounds(getX(), getY(), EXPLODE_WIDTH, EXPLODE_HEIGHT);
				setRegion(explodeAnim.getKeyFrame(animTimer));
				break;
			case END:
				break;
		}
		prevParentMoveState = myFrameInput.moveState;
		applyFrameInput(frameInput);
	}
}
