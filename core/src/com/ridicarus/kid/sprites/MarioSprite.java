package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.player.MarioRole.MarioRoleState;

public class MarioSprite extends Sprite {
	private static final float BLINK_DURATION = 0.05f;

	public enum MarioSpriteState { FALLING, GROWING, JUMPING, STANDING, RUNNING, DEAD, BRAKING, SHRINKING };
	private MarioSpriteState curState;

	private TextureRegion marioStand;
	private TextureRegion bigMarioStand;
	private TextureRegion marioJump;
	private TextureRegion bigMarioJump;
	private TextureRegion marioBrake;
	private TextureRegion bigMarioBrake;
	private Animation<TextureRegion> marioRun;
	private Animation<TextureRegion> bigMarioRun;
	private Animation<TextureRegion> growMario;
	private Animation<TextureRegion> shrinkMario;
	private TextureRegion marioDead;

	private float stateTimer;
	private float fallStartStateTime;
	private boolean wasBig;
	private boolean runGrowAnimation;
	private boolean runShrinkAnimation;
	private boolean isBlinking;
	private float blinkTimer;

	public MarioSprite(TextureAtlas atlas, MarioRoleState stateIn, boolean isBig, boolean facingRight,
			boolean isDmgInvincible) {
		super(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO));

		Array<TextureRegion> frames;
		
		isBlinking = false;
		blinkTimer = 0f;

		frames = new Array<TextureRegion>();
		for(int i = 1; i<4; i++)
			frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), i*16, 0, 16, 16));
		marioRun = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		for(int i = 1; i<4; i++)
			frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), i*16, 0, 16, 32));
		bigMarioRun = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		// create animation for mario growth (he gets big, gets small again, then finishes big)
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		growMario = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		// Create animation for mario shrink (he changes to jump mario, then big mario stand, gets small,
		// gets big again, ..., then finishes small)
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 5 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 15 * 16, 0, 16, 32));
		shrinkMario = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		marioJump = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 5 * 16, 0, 16, 16);
		bigMarioJump = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 5 * 16, 0, 16, 32);

		marioStand = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 0, 0, 16, 16);
		bigMarioStand = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32);

		marioDead = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 6 * 16, 0, 16, 16);

		marioBrake = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 4 * 16, 0, 16, 16);
		bigMarioBrake = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 4 * 16, 0, 16, 32);

		setBounds(0, 0, GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setRegion(marioStand);

		curState = MarioSpriteState.STANDING;
		stateTimer = 0f;
		fallStartStateTime = 0f;

		wasBig = isBig;
		runGrowAnimation = false;
		runShrinkAnimation = false;
		update(0f, new Vector2(0f, 0f), stateIn, isBig, facingRight, isDmgInvincible);
	}

	public void update(float delta, Vector2 position, MarioRoleState stateIn, boolean isBig, boolean facingRight,
			boolean isDmgInvincible) {
		MarioSpriteState prevState;

		// switch from small to big?
		if(isBig && !wasBig) {
			// TODO: still need to test that mario can switch from shrinking to growing at the same time
			if(runShrinkAnimation)
				runShrinkAnimation = false;

			runGrowAnimation = true;
			stateTimer = 0f;
			setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y)*2);
		}
		else if(!isBig && wasBig) {
			if(runGrowAnimation)
				runGrowAnimation = false;

			runShrinkAnimation = true;
			stateTimer = 0f;
			// setBounds when shrink animation finishes
		}

		if(runGrowAnimation && growMario.isAnimationFinished(stateTimer))
			runGrowAnimation = false;

		if(runShrinkAnimation && shrinkMario.isAnimationFinished(stateTimer)) {
			runShrinkAnimation = false;
			setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		}

		prevState = curState;
		curState = getState(stateIn, isBig);
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		wasBig = isBig;

		// mario's state (run, walk, etc.) might change while blinking, so a separate timer is needed for blinking
		isBlinking = isDmgInvincible;
		if(isBlinking)
			blinkTimer += delta;
		else
			blinkTimer = 0f;

		setRegion(getFrame(delta, isBig, facingRight));

		// if shrinking then the sprite needs to be offset a little higher
		if(curState == MarioSpriteState.SHRINKING)
			setPosition(position.x - getWidth() / 2, position.y - getHeight() / 4);
		else
			setPosition(position.x - getWidth() / 2, position.y - getHeight() / 2);
	}

	private MarioSpriteState getState(MarioRoleState marioState, boolean isBig) {
		MarioSpriteState stateOut;

		// if not dead, and mario size changed from small to big then run grow animation
		if(marioState != MarioRoleState.DEAD) {
			if(runGrowAnimation && !growMario.isAnimationFinished(stateTimer))
				return MarioSpriteState.GROWING;
			else if(runShrinkAnimation && !shrinkMario.isAnimationFinished(stateTimer))
				return MarioSpriteState.SHRINKING;
		}

		switch(marioState) {
			case RUN:
				stateOut = MarioSpriteState.RUNNING;
				break;
			case JUMP:
				stateOut = MarioSpriteState.JUMPING;
				break;
			case FALL:
				stateOut = MarioSpriteState.STANDING;
				break;
			case BRAKE:
				stateOut = MarioSpriteState.BRAKING;
				break;
			case DEAD:
				stateOut = MarioSpriteState.DEAD;
				break;
			case STAND:
			default:
				stateOut = MarioSpriteState.STANDING;
				break;
		}
		return stateOut;
	}

	private TextureRegion getFrame(float delta, boolean isBig, boolean facingRight) {
		TextureRegion region;

		switch(curState) {
			case RUNNING:
				region = isBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
				break;
			case JUMPING:
				region = isBig ? bigMarioJump : marioJump;
				break;
			case FALLING:
				if(curState == MarioSpriteState.STANDING)
					fallStartStateTime = -1;
				else if(curState == MarioSpriteState.RUNNING)
					fallStartStateTime = stateTimer;

				// mario maintains his current frame of run animation if he was running when started to fall
				if(fallStartStateTime == -1)
					region = isBig ? bigMarioStand : marioStand;
				else {
					region = isBig ? bigMarioRun.getKeyFrame(fallStartStateTime, true) :
						marioRun.getKeyFrame(fallStartStateTime, true);
				}
				break;
			case BRAKING:
				region = isBig ? bigMarioBrake : marioBrake;
				break;
			case DEAD:
				region = marioDead;
				break;
			case GROWING:
				region = growMario.getKeyFrame(stateTimer);
				break;
			case SHRINKING:
				region = shrinkMario.getKeyFrame(stateTimer);
				break;
			case STANDING:
			default:
				region = isBig ? bigMarioStand : marioStand;
				break;
		}

		// do we need to flip left/right?
		if((facingRight && region.isFlipX()) || (!facingRight && !region.isFlipX()))
			region.flip(true,  false);

		return region;
	}

	@Override
	public void draw(Batch batch) {
		boolean visible;

		visible = true;
		if(isBlinking) {
			if(Math.floorMod((int) ((float) blinkTimer / BLINK_DURATION), 2) == 0)
				visible = false;
		}

		if(visible)
			super.draw(batch);
	}
}
