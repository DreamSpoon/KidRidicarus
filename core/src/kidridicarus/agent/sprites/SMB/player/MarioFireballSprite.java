package kidridicarus.agent.sprites.SMB.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.agent.SMB.player.MarioFireball.FireballState;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class MarioFireballSprite extends Sprite {
	private static final float ANIM_SPEED_FLY = 0.2f;
	private static final float ANIM_SPEED_EXP = 0.1f;
	private Animation<TextureRegion> ballAnimation;
	private Animation<TextureRegion> explodeAnimation;
	private float stateTimer;
	private FireballState prevState;

	public MarioFireballSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL, 0, 0, 8, 8));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL, 8, 0, 8, 8));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL, 16, 0, 8, 8));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL, 24, 0, 8, 8));
		ballAnimation = new Animation<TextureRegion>(ANIM_SPEED_FLY, frames);
		frames.clear();

		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL_EXP, 0, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL_EXP, 16, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREBALL_EXP, 32, 0, 16, 16));
		explodeAnimation = new Animation<TextureRegion>(ANIM_SPEED_EXP, frames);

		setRegion(ballAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(8), UInfo.P2M(8));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer = 0f;
		prevState = FireballState.FLY;
	}

	public void update(float delta, Vector2 position, FireballState curState) {
		// change the size of the sprite when it changes to an explosion
		if(curState == FireballState.EXPLODE && prevState != FireballState.EXPLODE)
			setBounds(getX(), getY(), UInfo.P2M(16), UInfo.P2M(16));

		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		prevState = curState;

		switch(curState) {
			case FLY:
				setRegion(ballAnimation.getKeyFrame(stateTimer, true));
				break;
			case EXPLODE:
				setRegion(explodeAnimation.getKeyFrame(stateTimer, false));
				break;
		}

		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);

	}

	public boolean isExplodeFinished() {
		return (explodeAnimation.isAnimationFinished(stateTimer) && prevState == FireballState.EXPLODE);
	}
}
