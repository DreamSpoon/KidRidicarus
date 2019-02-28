package kidridicarus.game.agentsprite.SMB.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.SMB.player.MarioFireball.FireballState;
import kidridicarus.game.info.SMBAnim;

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
	private FireballState prevState;

	public MarioFireballSprite(TextureAtlas atlas, Vector2 position) {
		ballAnim = new Animation<TextureRegion>(ANIM_SPEED_FLY,
				atlas.findRegions(SMBAnim.General.FIREBALL), PlayMode.LOOP);

		explodeAnim = new Animation<TextureRegion>(ANIM_SPEED_EXP,
				atlas.findRegions(SMBAnim.General.FIREBALL_EXP), PlayMode.LOOP);

		setRegion(ballAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPR_BALLWIDTH, SPR_BALLHEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer = 0f;
		prevState = FireballState.FLY;
	}

	public void update(float delta, Vector2 position, FireballState curState) {
		// change the size of the sprite when it changes to an explosion
		if(curState == FireballState.EXPLODE && prevState != FireballState.EXPLODE)
			setBounds(getX(), getY(), SPR_EXPWIDTH, SPR_EXPHEIGHT);

		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;

		switch(curState) {
			case FLY:
				setRegion(ballAnim.getKeyFrame(stateTimer, true));
				break;
			case EXPLODE:
				setRegion(explodeAnim.getKeyFrame(stateTimer, false));
				break;
		}

		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);

	}

	public boolean isExplodeFinished() {
		return (explodeAnim.isAnimationFinished(stateTimer) && prevState == FireballState.EXPLODE);
	}
}
