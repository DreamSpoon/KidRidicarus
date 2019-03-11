package kidridicarus.game.SMB.agentsprite.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB.agent.player.Mario.MarioPowerState;
import kidridicarus.game.SMB.agent.player.Mario.MarioAgentState;
import kidridicarus.game.SMB.agent.player.Mario.MarioBodyState;

public class MarioSprite extends Sprite {
	private static final float SMLSPR_WIDTH = UInfo.P2M(16);
	private static final float SMLSPR_HEIGHT = UInfo.P2M(16);
	private static final float BIGSPR_WIDTH = UInfo.P2M(16);
	private static final float BIGSPR_HEIGHT = UInfo.P2M(32);
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
	// big has exactly 1 more group than small, but the number are the same, sortof...
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

	public enum MarioSpriteState { STAND, RUN, JUMP, BRAKE, FALL, SHRINK, GROW, DUCK, FIREBALL, DEAD, CLIMB }

	private MarioSpriteState curSpriteState;

	private Animation<TextureRegion>[][] smlAnim;
	private Animation<TextureRegion>[][] bigAnim;

	private float stateTimer;
	private float fallStateStartTime;
	private boolean wasBigLastFrame;
	private boolean doGrowAnim;
	private boolean doShrinkAnim;
	private boolean doFireballAnim;
	private float starPowerFrameTimer;
	private float climbStateTimer;

	public MarioSprite(TextureAtlas atlas, Vector2 position, MarioPowerState powerState) {
		createAnimations(atlas);

		setRegion(smlAnim[STAND_POSE][BIG_REG_GRP].getKeyFrame(0f));
		setBounds(0f, 0f, SMLSPR_WIDTH, SMLSPR_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		curSpriteState = MarioSpriteState.STAND;
		stateTimer = 0f;
		fallStateStartTime = 0f;

		wasBigLastFrame = (powerState != MarioPowerState.SMALL);
		doGrowAnim = false;
		doShrinkAnim = false;
		doFireballAnim = false;
		starPowerFrameTimer = 0f;
		climbStateTimer = 0f;
	}

	@SuppressWarnings("unchecked")
	private void createAnimations(TextureAtlas atlas) {
		// allocate the arrays for animations
		smlAnim = (Animation<TextureRegion>[][]) new Animation[NUM_POSES][];
		bigAnim = (Animation<TextureRegion>[][]) new Animation[NUM_POSES][];
		for(int i=0; i<NUM_POSES; i++) {
			smlAnim[i] = (Animation<TextureRegion>[]) new Animation[SML_NUM_GRPS];
			bigAnim[i] = (Animation<TextureRegion>[]) new Animation[BIG_NUM_GRPS];
		}

		// brake
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[BRAKE_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/sml/mario_" + GRP_NAMES[i] + "_brake"), PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[BRAKE_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_brake"), PlayMode.LOOP);

		// climb
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[CLIMB_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/sml/mario_" + GRP_NAMES[i] + "_climb"), PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[CLIMB_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_climb"), PlayMode.LOOP);

		// dead
		smlAnim[DEAD_POSE][SML_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/sml/mario_reg_dead"), PlayMode.LOOP);

		// duck
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[DUCK_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_duck"), PlayMode.LOOP);

		// grow
		bigAnim[GROW_POSE][BIG_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions("player/mario/big/mario_reg_grow"), PlayMode.NORMAL);

		// jump
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[JUMP_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/sml/mario_" + GRP_NAMES[i] + "_jump"), PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[JUMP_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_jump"), PlayMode.LOOP);

		// run
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[RUN_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/sml/mario_" + GRP_NAMES[i] + "_run"), PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[RUN_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_run"), PlayMode.LOOP);

		// shrink
		bigAnim[SHRINK_POSE][BIG_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions("player/mario/big/mario_reg_shrink"), PlayMode.NORMAL);

		// stand
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[STAND_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/sml/mario_" + GRP_NAMES[i] + "_stand"), PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[STAND_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_stand"), PlayMode.LOOP);

		// throw fireball
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[THROW_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions("player/mario/big/mario_" + GRP_NAMES[i] + "_throw"), PlayMode.LOOP);
	}

	public void update(float delta, Vector2 position, MarioAgentState agentState, MarioBodyState bodyState,
			MarioPowerState powerState, boolean facingRight, boolean isStarPowered, boolean isBigBody,
			Direction4 moveDir) {
		MarioSpriteState prevSpriteState;

		// switch from small to big?
		if(powerState != MarioPowerState.SMALL && !wasBigLastFrame) {
			// TODO: still need to test that mario can switch from shrinking to growing at the same time
			if(doShrinkAnim)
				doShrinkAnim = false;

			doGrowAnim = true;
			stateTimer = 0f;
			setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
		}
		else if(powerState == MarioPowerState.SMALL && wasBigLastFrame) {
			if(doGrowAnim)
				doGrowAnim = false;

			doShrinkAnim = true;
			stateTimer = 0f;
			// setBounds when shrink animation finishes
		}

		if(doGrowAnim && bigAnim[GROW_POSE][BIG_REG_GRP].isAnimationFinished(stateTimer))
			doGrowAnim = false;

		if(doShrinkAnim && bigAnim[SHRINK_POSE][BIG_REG_GRP].isAnimationFinished(stateTimer)) {
			doShrinkAnim = false;
			// change sprite bounds to small mario size
			setBounds(getX(), getY(), SMLSPR_WIDTH, SMLSPR_HEIGHT);
		}

		if(doFireballAnim && bigAnim[THROW_POSE][BIG_FIRE_GRP].isAnimationFinished(stateTimer))
			doFireballAnim = false;

		prevSpriteState = curSpriteState;
		curSpriteState = getNextSpriteState(bodyState, agentState);

		if(prevSpriteState == MarioSpriteState.STAND && curSpriteState == MarioSpriteState.FALL)
			fallStateStartTime = -1;
		else if(prevSpriteState == MarioSpriteState.RUN && curSpriteState == MarioSpriteState.FALL)
			fallStateStartTime = stateTimer;

		if(curSpriteState == MarioSpriteState.DEAD && curSpriteState != prevSpriteState)
			setBounds(getX(), getY(), SMLSPR_WIDTH, SMLSPR_HEIGHT);

		stateTimer = curSpriteState == prevSpriteState ? stateTimer+delta : 0f;
		wasBigLastFrame = (powerState != MarioPowerState.SMALL);

		starPowerFrameTimer += delta;

		// if moving up/down then increment/decrement the climb timer, otherwise no change
		if(moveDir != null) {
			if(moveDir == Direction4.UP)
				climbStateTimer += delta;
			else if(moveDir == Direction4.DOWN) {
				// defaulting to SML_REG_GRP because climb animation should be same duration for all groups
				climbStateTimer = CommonInfo.ensurePositive(climbStateTimer - delta,
						smlAnim[CLIMB_POSE][SML_REG_GRP].getAnimationDuration());
			}
		}

		setRegion(getFrame(powerState, facingRight, isStarPowered));

		// if mario's body is small, but his sprite is big (the shrink animation is a big sprite) then offset
		if(!isBigBody && (powerState != MarioPowerState.SMALL || doShrinkAnim))
			setPosition(position.x - getWidth() / 2f, position.y - getHeight() / 2f + getHeight() / 4f);
		// otherwise center the sprite on mario's body
		else
			setPosition(position.x - getWidth() / 2f, position.y - getHeight() / 2f);
	}

	private MarioSpriteState getNextSpriteState(MarioBodyState bodyState, MarioAgentState agentState) {
		if(agentState != MarioAgentState.PLAY && agentState != MarioAgentState.FIREBALL) {
			switch(agentState) {
				case DEAD:
					return MarioSpriteState.DEAD;
				default:
					return MarioSpriteState.STAND;
			}
		}
		else {
			// some animations override the current mario state, unless mario is dead
			// (note: the animation might just be one frame, but displayed for a duration)
			if(agentState != MarioAgentState.DEAD) {
				if(agentState == MarioAgentState.FIREBALL)
					doFireballAnim = true;

				if(doGrowAnim)
					return MarioSpriteState.GROW;
				else if(doShrinkAnim)
					return MarioSpriteState.SHRINK;
				else if(doFireballAnim)
					return MarioSpriteState.FIREBALL;
			}
		}

		switch(bodyState) {
			case CLIMB:
				return MarioSpriteState.CLIMB;
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

	private TextureRegion getFrame(MarioPowerState powerState, boolean facingRight, boolean isStarPowered) {
		TextureRegion region = null;
		int grp;

		switch(powerState) {
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
			grp = getStarFrameGrp(powerState);

		// create an alias to the big or small animation arrays
		Animation<TextureRegion>[][] sizeAnim;
		if(powerState == MarioPowerState.SMALL)
			sizeAnim = smlAnim;
		else
			sizeAnim = bigAnim;
		// set region to an animation based on the current state
		switch(curSpriteState) {
			case CLIMB:
				region = sizeAnim[CLIMB_POSE][grp].getKeyFrame(climbStateTimer);
				break;
			case DUCK:
				region = bigAnim[DUCK_POSE][grp].getKeyFrame(stateTimer);
				break;
			case FIREBALL:
				region = bigAnim[THROW_POSE][grp].getKeyFrame(stateTimer);
				break;
			case RUN:
				region = sizeAnim[RUN_POSE][grp].getKeyFrame(stateTimer);
				break;
			case JUMP:
				region = sizeAnim[JUMP_POSE][grp].getKeyFrame(stateTimer);
				break;
			case FALL:
				// mario maintains his current frame of run animation if he was running when started to fall
				if(fallStateStartTime == -1)
					region = sizeAnim[STAND_POSE][grp].getKeyFrame(stateTimer);
				else
					region = sizeAnim[RUN_POSE][grp].getKeyFrame(fallStateStartTime);
				break;
			case BRAKE:
				region = sizeAnim[BRAKE_POSE][grp].getKeyFrame(stateTimer);
				break;
			case DEAD:
				region = smlAnim[DEAD_POSE][SML_REG_GRP].getKeyFrame(stateTimer);
				break;
			case GROW:
				region = bigAnim[GROW_POSE][BIG_REG_GRP].getKeyFrame(stateTimer);
				break;
			case SHRINK:
				region = bigAnim[SHRINK_POSE][BIG_REG_GRP].getKeyFrame(stateTimer);
				break;
			case STAND:
				region = sizeAnim[STAND_POSE][grp].getKeyFrame(stateTimer);
				break;
		}

		// should the sprite be flipped on X due to facing direction?
		if((facingRight && region.isFlipX()) || (!facingRight && !region.isFlipX()))
			region.flip(true,  false);

		return region;
	}

	private int getStarFrameGrp(MarioPowerState powerState) {
		switch(Math.floorMod((int) (starPowerFrameTimer / STARPOWER_ANIM_SPEED), NUM_STARPOWER_FRAMES)) {
			case 3:
				switch(powerState) {
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
}
