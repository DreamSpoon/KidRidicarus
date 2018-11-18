package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.player.MarioRole.MarioCharState;
import com.ridicarus.kid.roles.player.MarioRole.MarioPowerState;

public class MarioSprite extends Sprite {
	private static final float BLINK_DURATION = 0.05f;

	public enum MarioSpriteState { FALLING, GROWING, JUMPING, STANDING, RUNNING, DIE, BRAKING, SHRINKING, FIREBALL };
	private MarioSpriteState curState;

	private TextureRegion marioStand;
	private TextureRegion bigMarioStand;
	private TextureRegion fireMarioStand;
	private Animation<TextureRegion> marioRun;
	private Animation<TextureRegion> bigMarioRun;
	private Animation<TextureRegion> fireMarioRun;
	private TextureRegion marioJump;
	private TextureRegion bigMarioJump;
	private TextureRegion fireMarioJump;
	private TextureRegion marioBrake;
	private TextureRegion bigMarioBrake;
	private TextureRegion fireMarioBrake;
	private Animation<TextureRegion> growMario;
	private Animation<TextureRegion> shrinkMario;
	private TextureRegion marioDead;
	private Animation<TextureRegion> fireballMario;

	private float stateTimer;
	private float fallStartStateTime;
	private boolean wasBig;
	private boolean runGrowAnimation;
	private boolean runShrinkAnimation;
	private boolean isBlinking;
	private float blinkTimer;
	private boolean runFireballAnimation;

	public MarioSprite(TextureAtlas atlas, MarioCharState stateIn, MarioPowerState subState, boolean facingRight) {
		super(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO));

		Array<TextureRegion> frames;

		isBlinking = false;
		blinkTimer = 0f;

		marioStand = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 0, 0, 16, 16);
		bigMarioStand = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 0, 0, 16, 32);
		fireMarioStand = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_FIREMARIO), 0, 0, 16, 32);

		frames = new Array<TextureRegion>();
		for(int i = 1; i<4; i++)
			frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), i*16, 0, 16, 16));
		marioRun = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		for(int i = 1; i<4; i++)
			frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), i*16, 0, 16, 32));
		bigMarioRun = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		for(int i = 1; i<4; i++)
			frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_FIREMARIO), i*16, 0, 16, 32));
		fireMarioRun = new Animation<TextureRegion>(0.1f, frames);
		frames.clear();

		marioJump = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 5 * 16, 0, 16, 16);
		bigMarioJump = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 5 * 16, 0, 16, 32);
		fireMarioJump = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_FIREMARIO), 5 * 16, 0, 16, 32);

		marioBrake = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 4 * 16, 0, 16, 16);
		bigMarioBrake = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BIGMARIO), 4 * 16, 0, 16, 32);
		fireMarioBrake = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_FIREMARIO), 4 * 16, 0, 16, 32);

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

		marioDead = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_LITTLEMARIO), 6 * 16, 0, 16, 16);

		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_FIREMARIO), 16 * 16, 0, 16, 32));
		fireballMario = new Animation<TextureRegion>(0.15f, frames);
		frames.clear();

		setBounds(0, 0, GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setRegion(marioStand);

		curState = MarioSpriteState.STANDING;
		stateTimer = 0f;
		fallStartStateTime = 0f;

		wasBig = (subState != MarioPowerState.SMALL);
		runGrowAnimation = false;
		runShrinkAnimation = false;
		runFireballAnimation = false;
		update(0f, new Vector2(0f, 0f), stateIn, subState, facingRight, false);
	}

	public void update(float delta, Vector2 position, MarioCharState stateIn, MarioPowerState subState, boolean facingRight,
			boolean isDmgInvincible) {
		MarioSpriteState prevState;

		// switch from small to big?
		if(subState != MarioPowerState.SMALL && !wasBig) {
			// TODO: still need to test that mario can switch from shrinking to growing at the same time
			if(runShrinkAnimation)
				runShrinkAnimation = false;

			runGrowAnimation = true;
			stateTimer = 0f;
			setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y)*2);
		}
		else if(subState == MarioPowerState.SMALL && wasBig) {
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

		if(runFireballAnimation && fireballMario.isAnimationFinished(stateTimer))
			runFireballAnimation = false;

		prevState = curState;
		curState = getState(stateIn, subState);
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		wasBig = (subState != MarioPowerState.SMALL);

		// mario's state (run, walk, etc.) might change while blinking, so a separate timer is needed for blinking
		isBlinking = isDmgInvincible;
		if(isBlinking)
			blinkTimer += delta;
		else
			blinkTimer = 0f;

		setRegion(getFrame(delta, subState, facingRight));

		// if shrinking then the sprite needs to be offset a little higher
		if(curState == MarioSpriteState.SHRINKING)
			setPosition(position.x - getWidth() / 2, position.y - getHeight() / 4);
		else
			setPosition(position.x - getWidth() / 2, position.y - getHeight() / 2);
	}

	// sprite state includes growing and shrinking states, which are not in the mario char state
	private MarioSpriteState getState(MarioCharState marioState, MarioPowerState subState) {
		MarioSpriteState stateOut = null;

		// some animations override the current mario state, unless mario is dead
		// (note: the animation might just be one frame, but displayed for a duration)
		if(marioState != MarioCharState.DIE) {
			if(runGrowAnimation)
				return MarioSpriteState.GROWING;
			else if(runShrinkAnimation)
				return MarioSpriteState.SHRINKING;
			else if(runFireballAnimation)
				return MarioSpriteState.FIREBALL;
		}

		switch(marioState) {
			case FIREBALL:
				runFireballAnimation = true;
				stateOut = MarioSpriteState.FIREBALL;
				break;
			case WALKRUN:
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
			case DIE:
				stateOut = MarioSpriteState.DIE;
				break;
			case STAND:
				stateOut = MarioSpriteState.STANDING;
				break;
		}
		return stateOut;
	}

	private TextureRegion getFrame(float delta, MarioPowerState subState, boolean facingRight) {
		TextureRegion region = null;

		switch(curState) {
			case FIREBALL:
				region = fireballMario.getKeyFrame(stateTimer, false);
				break;
			case RUNNING:
				if(subState == MarioPowerState.FIRE)
					region = fireMarioRun.getKeyFrame(stateTimer, true);
				else if(subState == MarioPowerState.BIG)
					region = bigMarioRun.getKeyFrame(stateTimer, true);
				else
					region = marioRun.getKeyFrame(stateTimer, true);
				break;
			case JUMPING:
				if(subState == MarioPowerState.FIRE)
					region = fireMarioJump;
				else if(subState == MarioPowerState.BIG)
					region = bigMarioJump;
				else
					region = marioJump;
				break;
			case FALLING:
				if(curState == MarioSpriteState.STANDING)
					fallStartStateTime = -1;
				else if(curState == MarioSpriteState.RUNNING)
					fallStartStateTime = stateTimer;

				// mario maintains his current frame of run animation if he was running when started to fall
				if(fallStartStateTime == -1) {
					if(subState == MarioPowerState.FIRE)
						region = fireMarioStand;
					else if(subState == MarioPowerState.BIG)
						region = bigMarioStand;
					else
						region = marioStand;
				}
				else {
					if(subState == MarioPowerState.FIRE)
						region = fireMarioRun.getKeyFrame(fallStartStateTime, true);
					else if(subState == MarioPowerState.BIG)
						region = bigMarioRun.getKeyFrame(fallStartStateTime, true);
					else
						region = marioRun.getKeyFrame(fallStartStateTime, true);
				}
				break;
			case BRAKING:
				if(subState == MarioPowerState.FIRE)
					region = fireMarioBrake;
				else if(subState == MarioPowerState.BIG)
					region = bigMarioBrake;
				else
					region = marioBrake;
				break;
			case DIE:
				region = marioDead;
				break;
			case GROWING:
				region = growMario.getKeyFrame(stateTimer);
				break;
			case SHRINKING:
				region = shrinkMario.getKeyFrame(stateTimer);
				break;
			case STANDING:
				if(subState == MarioPowerState.FIRE)
					region = fireMarioStand;
				else if(subState == MarioPowerState.BIG)
					region = bigMarioStand;
				else
					region = marioStand;
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
