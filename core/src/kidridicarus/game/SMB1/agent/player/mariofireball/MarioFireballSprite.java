package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireball.MoveState;
import kidridicarus.game.info.SMB1_Gfx;

public class MarioFireballSprite extends Sprite {
	private static final float SPR_BALLWIDTH = UInfo.P2M(8);
	private static final float SPR_BALLHEIGHT = UInfo.P2M(8);
	private static final float SPR_EXPWIDTH = UInfo.P2M(16);
	private static final float SPR_EXPHEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED_FLY = 0.2f;
	private static final float ANIM_SPEED_EXP = 0.1f;
	private Animation<TextureRegion> ballAnim;
	private Animation<TextureRegion> explodeAnim;
	private float stateTimer;
	private MoveState parentPrevMoveState;

	public MarioFireballSprite(TextureAtlas atlas, Vector2 position) {
		ballAnim = new Animation<TextureRegion>(ANIM_SPEED_FLY,
				atlas.findRegions(SMB1_Gfx.Player.MarioFireball.FIREBALL), PlayMode.LOOP);

		explodeAnim = new Animation<TextureRegion>(ANIM_SPEED_EXP,
				atlas.findRegions(SMB1_Gfx.Player.MarioFireball.FIREBALL_EXP), PlayMode.NORMAL);

		setRegion(ballAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPR_BALLWIDTH, SPR_BALLHEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer = 0f;
		parentPrevMoveState = null;
	}

	public void update(float delta, Vector2 position, MoveState parentMoveState) {
		boolean parentMoveStateChanged = parentMoveState != parentPrevMoveState;
		switch(parentMoveState) {
			case FLY:
			case DESPAWN:
				setRegion(ballAnim.getKeyFrame(stateTimer));
				break;
			case EXPLODE:
				if(parentMoveStateChanged) {
					// change the size of the sprite when it changes to an explosion
					setBounds(getX(), getY(), SPR_EXPWIDTH, SPR_EXPHEIGHT);
					setRegion(explodeAnim.getKeyFrame(0f));
				}
				else
					setRegion(explodeAnim.getKeyFrame(stateTimer));
				break;
		}
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		stateTimer = parentMoveStateChanged ? 0f : stateTimer+delta;
		parentPrevMoveState = parentMoveState;
	}

	public boolean isExplodeAnimFinished() {
		return parentPrevMoveState == MoveState.EXPLODE && explodeAnim.isAnimationFinished(stateTimer);
	}
}
