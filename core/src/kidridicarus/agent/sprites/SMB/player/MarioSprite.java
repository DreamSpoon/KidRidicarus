package kidridicarus.agent.sprites.SMB.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.SMB.player.Mario.MarioPowerState;
import kidridicarus.agent.SMB.player.Mario.MarioState;
import kidridicarus.agent.bodies.SMB.player.MarioBody.MarioBodyState;
import kidridicarus.info.UInfo;

public class MarioSprite extends Sprite {
	private static final float SMLSPR_WIDTH = 16;
	private static final float SMLSPR_HEIGHT = 16;
	private static final float BIGSPR_WIDTH = 16;
	private static final float BIGSPR_HEIGHT = 32;
	private static final float BLINK_DURATION = 0.05f;
	private static final float STARPOWER_ANIM_SPEED = 0.05f;
	private static final int NUM_STARPOWER_FRAMES = 4;
	private static final float REG_ANIM_SPEED = 0.1f;

	private static final int NUM_POSES = 10;
	private static final int STAND_POSE = 0;
	private static final int RUN_POSE = 1;
	private static final int JUMP_POSE = 2;
	private static final int BRAKE_POSE = 3;
	private static final int GROW_POSE = 4;
	private static final int SHRINK_POSE = 5;
	private static final int DUCK_POSE = 6;
	private static final int THROW_POSE = 7;
	private static final int DEAD_POSE = 8;
	private static final int CLIMB_POSE = 9;

	private static final String[] GRP_NAMES = new String[] { "reg", "inv1", "inv2", "inv3", "fire" };
	private static final int BIG_NUM_GRPS = 5;
	private static final int BIG_REG_GRP = 0;
	private static final int BIG_INV1_GRP = 1;
	private static final int BIG_INV2_GRP = 2;
	private static final int BIG_INV3_GRP = 3;
	private static final int BIG_FIRE_GRP = 4;
	private static final int SML_NUM_GRPS = 4;
	private static final int SML_REG_GRP = 0;
//	private static final int SML_INV1_GRP = 1;
//	private static final int SML_INV2_GRP = 2;
//	private static final int SML_INV3_GRP = 3;

	public enum MarioSpriteState { STAND, RUN, JUMP, BRAKE, FALL, SHRINK, GROW, DUCK, FIREBALL, DEAD, END_SLIDE,
		END_SLIDE_DONE, END_SLIDE_FALL };

	private MarioSpriteState curState;

	private Animation<TextureRegion>[][] smlAnim, bigAnim;

	private float stateTimer;
	private float fallStartStateTime;
	private boolean wasBigLastFrame;
	private boolean doGrowAnimation;
	private boolean doShrinkAnimation;
	private boolean isBlinking;
	private float blinkTimer;
	private boolean doFireballAnimation;
	private float starPowerFrameTimer;

	public MarioSprite(TextureAtlas atlas, Vector2 position, MarioBodyState stateIn, MarioPowerState subState,
			boolean facingRight) {
		isBlinking = false;
		blinkTimer = 0f;

		createAnimations(atlas);

		setRegion(smlAnim[STAND_POSE][BIG_REG_GRP].getKeyFrame(0f));
		setBounds(0, 0, UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		curState = MarioSpriteState.STAND;
		stateTimer = 0f;
		fallStartStateTime = 0f;

		wasBigLastFrame = (subState != MarioPowerState.SMALL);
		doGrowAnimation = false;
		doShrinkAnimation = false;
		doFireballAnimation = false;
		starPowerFrameTimer = 0f;
	}

	@SuppressWarnings("unchecked")
	private void createAnimations(TextureAtlas atlas) {
		// allocate the arrays
		smlAnim = (Animation<TextureRegion>[][]) new Animation[NUM_POSES][];
		bigAnim = (Animation<TextureRegion>[][]) new Animation[NUM_POSES][];
		for(int i=0; i<NUM_POSES; i++) {
			smlAnim[i] = (Animation<TextureRegion>[]) new Animation[SML_NUM_GRPS];
			bigAnim[i] = (Animation<TextureRegion>[]) new Animation[BIG_NUM_GRPS];
		}

		// brake
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[BRAKE_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_sml_" + GRP_NAMES[i] + "_brake"));
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[BRAKE_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_big_" + GRP_NAMES[i] + "_brake"));

		// climb
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[CLIMB_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_sml_" + GRP_NAMES[i] + "_climb"));
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[CLIMB_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_big_" + GRP_NAMES[i] + "_climb"));

		// dead
		smlAnim[DEAD_POSE][SML_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_sml_reg_dead"));

		// duck
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[DUCK_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_big_" + GRP_NAMES[i] + "_duck"));

		// grow
		bigAnim[GROW_POSE][BIG_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions("mario/mario_big_reg_grow"));

		// jump
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[JUMP_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_sml_" + GRP_NAMES[i] + "_jump"));
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[JUMP_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_big_" + GRP_NAMES[i] + "_jump"));

		// run
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[RUN_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_sml_" + GRP_NAMES[i] + "_run"));
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[RUN_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_big_" + GRP_NAMES[i] + "_run"));

		// shrink
		bigAnim[SHRINK_POSE][BIG_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions("mario/mario_big_reg_shrink"));

		// stand
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[STAND_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_sml_" + GRP_NAMES[i] + "_stand"));
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[STAND_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("mario/mario_big_" + GRP_NAMES[i] + "_stand"));

		// throw fireball
		bigAnim[THROW_POSE][BIG_FIRE_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions("mario/mario_big_fire_throw"));
	}

/*	@SuppressWarnings("unchecked")
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
		smlTempArray[REG_GRP] = SMBInfo.TA_SMB1_MARIO_REG_SML;
		smlTempArray[FIRE_GRP] = SMBInfo.TA_SMB1_SMLMARIO_FIRE;
		smlTempArray[INV1_GRP] = SMBInfo.TA_SMB1_MARIO_SML_INV1;
		smlTempArray[INV2_GRP] = SMBInfo.TA_SMB1_MARIO_SML_INV2;
		smlTempArray[INV3_GRP] = SMBInfo.TA_SMB1_MARIO_SML_INV3;
		bigTempArray = new String[NUM_GRPS];
		bigTempArray[REG_GRP] = SMBInfo.TA_SMB1_MARIO_BIG_REG;
		bigTempArray[FIRE_GRP] = SMBInfo.TA_SMB1_MARIO_BIG_FIRE;
		bigTempArray[INV1_GRP] = SMBInfo.TA_SMB1_MARIO_BIG_INV1;
		bigTempArray[INV2_GRP] = SMBInfo.TA_SMB1_MARIO_BIG_INV2;
		bigTempArray[INV3_GRP] = SMBInfo.TA_SMB1_MARIO_BIG_INV3;

		frames = new Array<TextureRegion>();

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(smlTempArray[i], 0, 0, 16, 16));
			anim[STAND_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			anim[STAND_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(smlTempArray[i], 1*16, 0, 16, 16));
			frames.add(atlas.findSubRegion(smlTempArray[i], 2*16, 0, 16, 16));
			frames.add(atlas.findSubRegion(smlTempArray[i], 3*16, 0, 16, 16));
			anim[RUN_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 1*16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 2*16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 3*16, 0, 16, 32));
			anim[RUN_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(smlTempArray[i], 5*16, 0, 16, 16));
			anim[JUMP_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 5*16, 0, 16, 32));
			anim[JUMP_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(smlTempArray[i], 4*16, 0, 16, 16));
			anim[BRAKE_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 4*16, 0, 16, 32));
			anim[BRAKE_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: no grow or shrink animations attached to small mario, only on the big mario
		for(i=0; i<NUM_GRPS; i++) {
			// for mario growth: he gets big, gets small again, then finishes big
			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			anim[GROW_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			// for mario shrink: he changes to jump mario, then big mario stand, gets small,
			// gets big again, ..., then finishes small
			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 5 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 0, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 15 * 16, 0, 16, 32));
			anim[SHRINK_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: only big mario has the duck anim
		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 6 * 16, 0, 16, 32));
			anim[DUCK_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: only small mario has the death anim
		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(smlTempArray[i], 6 * 16, 0, 16, 16));
			anim[DEAD_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		// NOTE: only big mario has the throw fireball anim
		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 16 * 16, 0, 16, 32));
			anim[FIREB_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}

		for(i=0; i<NUM_GRPS; i++) {
			frames.clear();
			frames.add(atlas.findSubRegion(smlTempArray[i], 7 * 16, 0, 16, 16));
			frames.add(atlas.findSubRegion(smlTempArray[i], 8 * 16, 0, 16, 16));
			anim[FLAG_POSE][SML_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);

			frames.clear();
			frames.add(atlas.findSubRegion(bigTempArray[i], 7 * 16, 0, 16, 32));
			frames.add(atlas.findSubRegion(bigTempArray[i], 8 * 16, 0, 16, 32));
			anim[FLAG_POSE][BIG_SIZE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED, frames);
		}
	}
*/

	public void update(float delta, Vector2 position, MarioState agentState, MarioBodyState bodyState,
			MarioPowerState powerState, boolean facingRight, boolean isDmgInvincible, boolean isStarPowered,
			boolean isBigBody) {
		MarioSpriteState prevState;

		// switch from small to big?
		if(powerState != MarioPowerState.SMALL && !wasBigLastFrame) {
			// TODO: still need to test that mario can switch from shrinking to growing at the same time
			if(doShrinkAnimation)
				doShrinkAnimation = false;

			doGrowAnimation = true;
			stateTimer = 0f;
			setBounds(getX(), getY(), UInfo.P2M(BIGSPR_WIDTH), UInfo.P2M(BIGSPR_HEIGHT));
		}
		else if(powerState == MarioPowerState.SMALL && wasBigLastFrame) {
			if(doGrowAnimation)
				doGrowAnimation = false;

			doShrinkAnimation = true;
			stateTimer = 0f;
			// setBounds when shrink animation finishes
		}

		if(doGrowAnimation && bigAnim[GROW_POSE][BIG_REG_GRP].isAnimationFinished(stateTimer))
			doGrowAnimation = false;

		if(doShrinkAnimation && bigAnim[SHRINK_POSE][BIG_REG_GRP].isAnimationFinished(stateTimer)) {
			doShrinkAnimation = false;
			// change sprite bounds to small mario size
			setBounds(getX(), getY(), UInfo.P2M(SMLSPR_WIDTH), UInfo.P2M(SMLSPR_HEIGHT));
		}

		if(doFireballAnimation && bigAnim[THROW_POSE][BIG_FIRE_GRP].isAnimationFinished(stateTimer))
			doFireballAnimation = false;

		prevState = curState;
		curState = getState(bodyState, powerState, agentState);

		if(prevState == MarioSpriteState.STAND && curState == MarioSpriteState.FALL)
			fallStartStateTime = -1;
		else if(prevState == MarioSpriteState.RUN && curState == MarioSpriteState.FALL)
			fallStartStateTime = stateTimer;

		if(curState == MarioSpriteState.DEAD && curState != prevState)
			setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
			
		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		wasBigLastFrame = (powerState != MarioPowerState.SMALL);

		// mario's state (run, walk, etc.) might change while blinking, so a separate timer is needed for blinking
		isBlinking = isDmgInvincible;
		if(isBlinking)
			blinkTimer += delta;
		else
			blinkTimer = 0f;

		starPowerFrameTimer += delta;

		setRegion(getFrame(powerState, facingRight, isStarPowered));

		// if mario's body is small, but his sprite is big (the shrink animation is a big sprite) then offset
		if(!isBigBody && (powerState != MarioPowerState.SMALL || doShrinkAnimation))
			setPosition(position.x - getWidth() / 2f, position.y - getHeight() / 2f + getHeight() / 4f);
		// otherwise center the sprite on mario's body
		else
			setPosition(position.x - getWidth() / 2f, position.y - getHeight() / 2f);
	}

	private MarioSpriteState getState(MarioBodyState bodyState, MarioPowerState subState, MarioState agentState) {
		if(agentState != MarioState.PLAY && agentState != MarioState.FIREBALL) {
			switch(agentState) {
				case DEAD:
					return MarioSpriteState.DEAD;
				case PIPE_ENTRYH:
				case PIPE_EXITH:
					return MarioSpriteState.RUN;
				case PIPE_ENTRYV:
				case PIPE_EXITV:
					return MarioSpriteState.STAND;
				case END1_SLIDE:
					return MarioSpriteState.END_SLIDE;
				case END2_WAIT1:
				case END3_WAIT2:
					return MarioSpriteState.END_SLIDE_DONE;
				case END4_FALL:
					return MarioSpriteState.END_SLIDE_FALL;
				case END5_BRAKE:
					return MarioSpriteState.BRAKE;
				case END6_RUN:
					return MarioSpriteState.RUN;
				case END99:	// NOTE: mario sprite is not drawn in state END99
				default:
					return MarioSpriteState.STAND;
			}
		}
		else {
			// some animations override the current mario state, unless mario is dead
			// (note: the animation might just be one frame, but displayed for a duration)
			if(agentState != MarioState.DEAD) {
				if(agentState == MarioState.FIREBALL)
					doFireballAnimation = true;

				if(doGrowAnimation)
					return MarioSpriteState.GROW;
				else if(doShrinkAnimation)
					return MarioSpriteState.SHRINK;
				else if(doFireballAnimation)
					return MarioSpriteState.FIREBALL;
			}
		}

		switch(bodyState) {
			case DUCK:
				return MarioSpriteState.DUCK;
			case WALKRUN:
				return MarioSpriteState.RUN;
			case JUMP:
				return MarioSpriteState.JUMP;
			case FALL:
				return MarioSpriteState.FALL;
			case BRAKE:
				return MarioSpriteState.BRAKE;
			case DEAD:
				return MarioSpriteState.DEAD;
			case STAND:
			default:
				return MarioSpriteState.STAND;
		}
	}

	private TextureRegion getFrame(MarioPowerState subState, boolean facingRight, boolean isStarPowered) {
		TextureRegion region = null;
		int grp;

		switch(subState) {
			case FIRE:
				grp = BIG_FIRE_GRP;
				break;
			case BIG:
				grp = BIG_REG_GRP;
				break;
			case SMALL:
			default:
				grp = SML_REG_GRP;
				break;
		}
		if(isStarPowered)
			grp = getStarFrameGrp(subState);

		// create an alias to the big or small animation arrays
		Animation<TextureRegion>[][] sizeAnim;
		if(subState == MarioPowerState.SMALL)
			sizeAnim = smlAnim;
		else
			sizeAnim = bigAnim;
		// set region to an animation based on the current state
		switch(curState) {
			case END_SLIDE_FALL:
				region = sizeAnim[RUN_POSE][grp].getKeyFrame(0f, false);
				break;
			case END_SLIDE_DONE:
				region = sizeAnim[CLIMB_POSE][grp].getKeyFrame(sizeAnim[CLIMB_POSE][grp].getAnimationDuration(), false);
				break;
			case END_SLIDE:
				region = sizeAnim[CLIMB_POSE][grp].getKeyFrame(stateTimer, true);
				break;
			case DUCK:
				region = bigAnim[DUCK_POSE][grp].getKeyFrame(stateTimer, false);
				break;
			case FIREBALL:
				region = bigAnim[THROW_POSE][grp].getKeyFrame(stateTimer, false);
				break;
			case RUN:
				region = sizeAnim[RUN_POSE][grp].getKeyFrame(stateTimer, true);
				break;
			case JUMP:
				region = sizeAnim[JUMP_POSE][grp].getKeyFrame(stateTimer, false);
				break;
			case FALL:
				// mario maintains his current frame of run animation if he was running when started to fall
				if(fallStartStateTime == -1)
					region = sizeAnim[STAND_POSE][grp].getKeyFrame(stateTimer, false);
				else
					region = sizeAnim[RUN_POSE][grp].getKeyFrame(fallStartStateTime, true);
				break;
			case BRAKE:
				region = sizeAnim[BRAKE_POSE][grp].getKeyFrame(stateTimer, false);
				break;
			case DEAD:
				region = smlAnim[DEAD_POSE][SML_REG_GRP].getKeyFrame(stateTimer, false);
				break;
			case GROW:
				region = bigAnim[GROW_POSE][BIG_REG_GRP].getKeyFrame(stateTimer, false);
				break;
			case SHRINK:
				region = bigAnim[SHRINK_POSE][BIG_REG_GRP].getKeyFrame(stateTimer, false);
				break;
			case STAND:
				region = sizeAnim[STAND_POSE][grp].getKeyFrame(stateTimer, false);
				break;
		}

		// should the sprite be flipped on X due to facing direciton?
		if((facingRight && region.isFlipX()) || (!facingRight && !region.isFlipX()))
			region.flip(true,  false);

		return region;
	}

	private int getStarFrameGrp(MarioPowerState subState) {
		switch(Math.floorMod((int) (starPowerFrameTimer / STARPOWER_ANIM_SPEED), NUM_STARPOWER_FRAMES)) {
			case 3:
				switch(subState) {
					case FIRE:
						return BIG_FIRE_GRP;
					case BIG:
					case SMALL:
					default:
						return BIG_REG_GRP;
				}
			case 2:
				return BIG_INV3_GRP;
			case 1:
				return BIG_INV2_GRP;
			case 0:
			default:
				return BIG_INV1_GRP;
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
