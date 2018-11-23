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
	private static final float STARPOWER_ANIM_SPEED = 0.05f;
	private static final int NUM_STARPOWER_FRAMES = 4;
	private static final float REG_ANIM_SPEED = 0.1f;

	public enum MarioSpriteState { STAND, RUN, JUMP, BRAKE, FALL, SHRINK, GROW, DUCK, FIREBALL, DEAD };
	private MarioSpriteState curState;

	private static final int NUM_POSES = 9;	// { STAND, RUN, JUMP, BRAKE, GROW, SHRINK, DUCK, FIREBALL, DEAD
	private static final int STAND_POSE = 0;
	private static final int RUN_POSE = 1;
	private static final int JUMP_POSE = 2;
	private static final int BRAKE_POSE = 3;
	private static final int GROW_POSE = 4;
	private static final int SHRINK_POSE = 5;
	private static final int DUCK_POSE = 6;
	private static final int FIREB_POSE = 7;
	private static final int DEAD_POSE = 8;

	private static final int NUM_SIZES = 2;
	private static final int SML_SIZE = 0;
	private static final int BIG_SIZE = 1;

	private static final int NUM_GRPS = 5;
	private static final int REG_GRP = 0;
	private static final int FIRE_GRP = 1;
	private static final int INV1_GRP = 2;
	private static final int INV2_GRP = 3;
	private static final int INV3_GRP = 4;

	private Animation<TextureRegion>[][][] anim;

	private float stateTimer;
	private float fallStartStateTime;
	private boolean wasBig;
	private boolean runGrowAnimation;
	private boolean runShrinkAnimation;
	private boolean isBlinking;
	private float blinkTimer;
	private boolean runFireballAnimation;
	private float starPowerFrameTimer;

	public MarioSprite(TextureAtlas atlas, Vector2 position, MarioCharState stateIn, MarioPowerState subState, boolean facingRight) {
		super(atlas.findRegion(GameInfo.TEXATLAS_SMLMARIO_REG));

		isBlinking = false;
		blinkTimer = 0f;

		createAnimations(atlas);

		setRegion(anim[STAND_POSE][SML_SIZE][REG_GRP].getKeyFrame(0f));
		setBounds(0, 0, GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		curState = MarioSpriteState.STAND;
		stateTimer = 0f;
		fallStartStateTime = 0f;

		wasBig = (subState != MarioPowerState.SMALL);
		runGrowAnimation = false;
		runShrinkAnimation = false;
		runFireballAnimation = false;
		starPowerFrameTimer = 0f;
	
		// DEBUG: why do update in constructor for this guy only? why not for turtle, fireball, etc.?
		// TODO: delete the following call to update()
//		update(0f, new Vector2(0f, 0f), stateIn, subState, facingRight, false, false);
	}

	@SuppressWarnings("unchecked")
	private void createAnimations(TextureAtlas atlas) {
		Array<TextureRegion> frames;
		String smlTempArray[], bigTempArray[];
		int i, j;

		// allocate the arrays
		anim = (Animation<TextureRegion>[][][]) new Animation[NUM_POSES][][];
		for(i=0; i<NUM_POSES; i++) {
			anim[i] = (Animation<TextureRegion>[][]) new Animation[NUM_SIZES][];
			for(j=0; j<NUM_SIZES; j++)
				anim[i][j] = (Animation<TextureRegion>[]) new Animation[NUM_GRPS];
		}

		// small and big sprites have different offsets in their sheets, so don't merge the two sizes
		smlTempArray = new String[NUM_GRPS];
		smlTempArray[REG_GRP] = GameInfo.TEXATLAS_SMLMARIO_REG;
		smlTempArray[FIRE_GRP] = GameInfo.TEXATLAS_SMLMARIO_FIRE;
		smlTempArray[INV1_GRP] = GameInfo.TEXATLAS_SMLMARIO_INV1;
		smlTempArray[INV2_GRP] = GameInfo.TEXATLAS_SMLMARIO_INV2;
		smlTempArray[INV3_GRP] = GameInfo.TEXATLAS_SMLMARIO_INV3;
		bigTempArray = new String[NUM_GRPS];
		bigTempArray[REG_GRP] = GameInfo.TEXATLAS_BIGMARIO_REG;
		bigTempArray[FIRE_GRP] = GameInfo.TEXATLAS_BIGMARIO_FIRE;
		bigTempArray[INV1_GRP] = GameInfo.TEXATLAS_BIGMARIO_INV1;
		bigTempArray[INV2_GRP] = GameInfo.TEXATLAS_BIGMARIO_INV2;
		bigTempArray[INV3_GRP] = GameInfo.TEXATLAS_BIGMARIO_INV3;

		frames = new Array<TextureRegion>();

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 0, 0, 16, 16));
			anim[STAND_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			anim[STAND_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 1*16, 0, 16, 16));
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 2*16, 0, 16, 16));
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 3*16, 0, 16, 16));
			anim[RUN_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 1*16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 2*16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 3*16, 0, 16, 32));
			anim[RUN_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 5*16, 0, 16, 16));
			anim[JUMP_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 5*16, 0, 16, 32));
			anim[JUMP_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 4*16, 0, 16, 16));
			anim[BRAKE_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 4*16, 0, 16, 32));
			anim[BRAKE_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: no grow or shrink animations attached to small mario, only on the big mario
		for(i=0; i<NUM_GRPS; i++) {
			// for mario growth: he gets big, gets small again, then finishes big
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			anim[GROW_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			// for mario shrink: he changes to jump mario, then big mario stand, gets small,
			// gets big again, ..., then finishes small
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 5 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 0, 0, 16, 32));
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 15 * 16, 0, 16, 32));
			anim[SHRINK_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: only big mario has the duck anim
		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 6 * 16, 0, 16, 32));
			anim[DUCK_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: only small mario has the death anim
		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(smlTempArray[i]), 6 * 16, 0, 16, 16));
			anim[DEAD_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: only big mario has the throw fireball anim
		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(new TextureRegion(atlas.findRegion(bigTempArray[i]), 16 * 16, 0, 16, 32));
			anim[FIREB_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}
	}

	public void update(float delta, Vector2 position, MarioCharState stateIn, MarioPowerState subState, boolean facingRight,
			boolean isDmgInvincible, boolean isStarPowered, float bodyFakeHeight) {
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

		if(runGrowAnimation && anim[GROW_POSE][BIG_SIZE][REG_GRP].isAnimationFinished(stateTimer))
			runGrowAnimation = false;

		if(runShrinkAnimation && anim[SHRINK_POSE][BIG_SIZE][REG_GRP].isAnimationFinished(stateTimer)) {
			runShrinkAnimation = false;
			// change sprite bounds to small mario size
			setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		}

		if(runFireballAnimation && anim[FIREB_POSE][BIG_SIZE][FIRE_GRP].isAnimationFinished(stateTimer))
			runFireballAnimation = false;

		prevState = curState;
		curState = getState(stateIn, subState);
		if(prevState == MarioSpriteState.STAND && curState == MarioSpriteState.FALL)
			fallStartStateTime = -1;
		else if(prevState == MarioSpriteState.RUN && curState == MarioSpriteState.FALL)
			fallStartStateTime = stateTimer;

		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		wasBig = (subState != MarioPowerState.SMALL);

		// mario's state (run, walk, etc.) might change while blinking, so a separate timer is needed for blinking
		isBlinking = isDmgInvincible;
		if(isBlinking)
			blinkTimer += delta;
		else
			blinkTimer = 0f;

		starPowerFrameTimer += delta;

		setRegion(getFrame(subState, facingRight, isStarPowered));

		// The sprite might be 32 pix tall while the body is 16 pix tall (e.g. ducking, shrinking), so some
		// height offset may be necessary. For more info, the expanded formula for y is:
		//     y = position.y - getHeight() / 2f + (getHeight() - bodyFakeHeight) / 2f;
		// I've just reduced
		setPosition(position.x - getWidth() / 2f, position.y - bodyFakeHeight / 2f);
	}

	// sprite state includes growing and shrinking states, which are not in the mario char state
	private MarioSpriteState getState(MarioCharState marioState, MarioPowerState subState) {
		MarioSpriteState stateOut = null;

		// some animations override the current mario state, unless mario is dead
		// (note: the animation might just be one frame, but displayed for a duration)
		if(marioState != MarioCharState.DEAD) {
			if(runGrowAnimation)
				return MarioSpriteState.GROW;
			else if(runShrinkAnimation)
				return MarioSpriteState.SHRINK;
			else if(runFireballAnimation)
				return MarioSpriteState.FIREBALL;
		}

		switch(marioState) {
			case DUCK:
				stateOut = MarioSpriteState.DUCK;
				break;
			case FIREBALL:
				runFireballAnimation = true;
				stateOut = MarioSpriteState.FIREBALL;
				break;
			case WALKRUN:
				stateOut = MarioSpriteState.RUN;
				break;
			case JUMP:
				stateOut = MarioSpriteState.JUMP;
				break;
			case FALL:
				stateOut = MarioSpriteState.FALL;
				break;
			case BRAKE:
				stateOut = MarioSpriteState.BRAKE;
				break;
			case DEAD:
				stateOut = MarioSpriteState.DEAD;
				break;
			case STAND:
				stateOut = MarioSpriteState.STAND;
				break;
		}
		return stateOut;
	}

	private TextureRegion getFrame(MarioPowerState subState, boolean facingRight, boolean isStarPowered) {
		TextureRegion region = null;
		int size, grp;

		switch(subState) {
			case FIRE:
				size = BIG_SIZE;
				grp = FIRE_GRP;
				break;
			case BIG:
				size = BIG_SIZE;
				grp = REG_GRP;
				break;
			case SMALL:
			default:
				size = SML_SIZE;
				grp = REG_GRP;
				break;
		}
		if(isStarPowered)
			grp = getStarFrameGrp(subState);

		switch(curState) {
			case DUCK:
				region = anim[DUCK_POSE][BIG_SIZE][grp].getKeyFrame(stateTimer, false);
				break;
			case FIREBALL:
				region = anim[FIREB_POSE][BIG_SIZE][grp].getKeyFrame(stateTimer, false);
				break;
			case RUN:
				region = anim[RUN_POSE][size][grp].getKeyFrame(stateTimer, true);
				break;
			case JUMP:
				region = anim[JUMP_POSE][size][grp].getKeyFrame(stateTimer, false);
				break;
			case FALL:
				// mario maintains his current frame of run animation if he was running when started to fall
				if(fallStartStateTime == -1)
					region = anim[STAND_POSE][size][grp].getKeyFrame(stateTimer, false);
				else
					region = anim[RUN_POSE][size][grp].getKeyFrame(fallStartStateTime, true);
				break;
			case BRAKE:
				region = anim[BRAKE_POSE][size][grp].getKeyFrame(stateTimer, false);
				break;
			case DEAD:
				region = anim[DEAD_POSE][SML_SIZE][grp].getKeyFrame(stateTimer, false);
				break;
			case GROW:
				region = anim[GROW_POSE][BIG_SIZE][grp].getKeyFrame(stateTimer, false);
				break;
			case SHRINK:
				region = anim[SHRINK_POSE][BIG_SIZE][grp].getKeyFrame(stateTimer, false);
				break;
			case STAND:
				region = anim[STAND_POSE][size][grp].getKeyFrame(stateTimer, false);
				break;
		}

		// do we need to flip left/right?
		if((facingRight && region.isFlipX()) || (!facingRight && !region.isFlipX()))
			region.flip(true,  false);

		return region;
	}

	private int getStarFrameGrp(MarioPowerState subState) {
		switch(Math.floorMod((int) (starPowerFrameTimer / STARPOWER_ANIM_SPEED), NUM_STARPOWER_FRAMES)) {
			case 3:
				switch(subState) {
					case FIRE:
						return FIRE_GRP;
					case BIG:
					case SMALL:
					default:
						return REG_GRP;
				}
			case 2:
				return INV3_GRP;
			case 1:
				return INV2_GRP;
			case 0:
			default:
				return INV1_GRP;
		}
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
