package kidridicarus.game.agent.SMB1.player.mario;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.SMB1.player.mario.Mario.MoveState;
import kidridicarus.game.agent.SMB1.player.mario.Mario.PowerState;
import kidridicarus.game.info.SMB1_Gfx;

public class MarioSprite extends Sprite {
	private static final float SMLSPR_WIDTH = UInfo.P2M(16);
	private static final float SMLSPR_HEIGHT = UInfo.P2M(16);
	private static final float BIGSPR_WIDTH = UInfo.P2M(16);
	private static final float BIGSPR_HEIGHT = UInfo.P2M(32);
	private static final float STARPOWER_ANIM_SPEED = 0.05f;
	private static final int NUM_STARPOWER_FRAMES = 4;
	private static final float REG_ANIM_SPEED = 0.1f;

	private static final float THROW_POSE_TIME = 0.15f;

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

	// big has exactly 1 more group than small, but the number are the same, sortof...
	private static final int BIG_NUM_GRPS = 5;
	private static final int BIG_REG_GRP = 0;
	private static final int BIG_INV1_GRP = 1;
	private static final int BIG_INV2_GRP = 2;
	private static final int BIG_INV3_GRP = 3;
	private static final int BIG_FIRE_GRP = 4;
	private static final int SML_NUM_GRPS = 4;
	private static final int SML_REG_GRP = 0;
	private static final int SML_INV1_GRP = 1;
	private static final int SML_INV2_GRP = 2;
	private static final int SML_INV3_GRP = 3;

	private static final Vector2 SPRITE_DUCK_OFFSET = UInfo.VectorP2M(0f, 8f);
	private static final float SHRINK_OFFSET_Y = UInfo.P2M(8);

	/*
	 * Animations by body size and [pose][group], where:
	 *   pose is stuff like "stand", "run", "jump", etc.
	 *   group is stuff like "small regular (red suit)", "big regular (red suit)", "big fire (white suit)",
	 *     "small star powered (shimmering suit)", etc.
	 */
	private Animation<TextureRegion>[][] smlAnim;
	private Animation<TextureRegion>[][] bigAnim;

	private MoveState prevParentMoveState;
	private PowerState prevParentPowerState;
	private float parentMoveStateTimer;
	private float throwPoseCooldown;
	private boolean isDrawAllowed;

	private enum SpriteState { NORMAL, GROW, SHRINK }
	private SpriteState spriteState;
	private float spriteStateTimer;
	private float starPowerTimer;
	private float climbAnimTimer;

	public MarioSprite(TextureAtlas atlas, Vector2 position, PowerState parentPowerState, boolean facingRight) {
		createAnimations(atlas);

		prevParentMoveState = MoveState.STAND;
		prevParentPowerState = parentPowerState;
		parentMoveStateTimer = 0f;
		throwPoseCooldown = 0f;
		isDrawAllowed = true;

		spriteState = SpriteState.NORMAL;
		spriteStateTimer = 0f;
		starPowerTimer = 0f;
		climbAnimTimer = 0f;

		// set the initial texture region and bounds
		switch(parentPowerState) {
			case SMALL:
				setRegion(smlAnim[STAND_POSE][SML_REG_GRP].getKeyFrame(0f));
				setBounds(getX(), getY(), SMLSPR_WIDTH, SMLSPR_HEIGHT);
				break;
			case BIG:
				setRegion(bigAnim[STAND_POSE][BIG_REG_GRP].getKeyFrame(0f));
				setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
				break;
			case FIRE:
				setRegion(bigAnim[STAND_POSE][BIG_FIRE_GRP].getKeyFrame(0f));
				setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
				break;
		}
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		// flip to face left if necessary
		if(!facingRight && !isFlipX())
			flip(true, false);
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
					atlas.findRegions(SMB1_Gfx.Player.Mario.SML_BRAKE + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[BRAKE_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_BRAKE + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);

		// climb
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[CLIMB_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.SML_CLIMB + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[CLIMB_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_CLIMB + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);

		// dead
		smlAnim[DEAD_POSE][SML_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.SML_DEAD), PlayMode.LOOP);

		// duck
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[DUCK_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_DUCK + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);

		// grow
		bigAnim[GROW_POSE][BIG_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_GROW), PlayMode.NORMAL);

		// jump
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[JUMP_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.SML_JUMP + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[JUMP_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_JUMP + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);

		// run
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[RUN_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.SML_RUN + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[RUN_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_RUN + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);

		// shrink
		bigAnim[SHRINK_POSE][BIG_REG_GRP] = new Animation<TextureRegion>(REG_ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_SHRINK), PlayMode.NORMAL);

		// stand
		for(int i=0; i<SML_NUM_GRPS; i++)
			smlAnim[STAND_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.SML_STAND + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[STAND_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_STAND + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);

		// throw fireball
		for(int i=0; i<BIG_NUM_GRPS; i++)
			bigAnim[THROW_POSE][i] = new Animation<TextureRegion>(REG_ANIM_SPEED,
					atlas.findRegions(SMB1_Gfx.Player.Mario.BIG_THROW + SMB1_Gfx.Player.Mario.GRP_STR[i]),
					PlayMode.LOOP);
	}

	public void update(float delta, Vector2 position, MoveState parentMoveState, PowerState parentPowerState,
			boolean facingRight, boolean didShootFireball, boolean isBlinking, boolean isStarPowered,
			Direction4 moveDir) {
		SpriteState nextSpriteState = getNextSpriteState(parentPowerState);
		boolean spriteStateChanged = nextSpriteState != spriteState;
		switch(nextSpriteState) {
			case NORMAL:
				processPowerState(delta, position, parentMoveState, parentPowerState, didShootFireball,
						isStarPowered, moveDir);
				break;
			case GROW:
				if(spriteStateChanged)
					setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
				processGrowState(position);
				break;
			case SHRINK:
				processShrinkState(position);
				break;
		}

		// flip to face left if needed
		if(!facingRight && !isFlipX())
			flip(true, false);

		// if blinking due to damage invulnerability, then flicker the sprite
		if(isBlinking && isDrawAllowed)
			isDrawAllowed = false;
		else
			isDrawAllowed = true;

		prevParentPowerState = parentPowerState;

		parentMoveStateTimer = parentMoveState == prevParentMoveState ? parentMoveStateTimer+delta : 0f;
		prevParentMoveState = parentMoveState;

		spriteStateTimer = nextSpriteState == spriteState ? spriteStateTimer+delta : 0f;
		spriteState = nextSpriteState;
	}

	private SpriteState getNextSpriteState(PowerState parentPowerState) {
		// did parent grow? if so then do grow anim
		if(parentPowerState.isBigBody() && !prevParentPowerState.isBigBody())
			return SpriteState.GROW;
		// did parent shrink? if so then do shrink anim
		else if(!parentPowerState.isBigBody() && prevParentPowerState.isBigBody())
			return SpriteState.SHRINK;
		else if(spriteState == SpriteState.GROW) {
			if(bigAnim[GROW_POSE][BIG_REG_GRP].isAnimationFinished(spriteStateTimer))
				return SpriteState.NORMAL;
			else
				return SpriteState.GROW;
		}
		else if(spriteState == SpriteState.SHRINK) {
			if(bigAnim[SHRINK_POSE][BIG_REG_GRP].isAnimationFinished(spriteStateTimer))
				return SpriteState.NORMAL;
			else
				return SpriteState.SHRINK;
		}
		else
			return SpriteState.NORMAL;
	}

	private void processPowerState(float delta, Vector2 position, MoveState parentMoveState,
			PowerState parentPowerState, boolean didShootFireball, boolean isStarPowered, Direction4 moveDir) {
		int group = SML_REG_GRP;

		switch(parentPowerState) {
			case SMALL:
				group = SML_REG_GRP;
				setBounds(getX(), getY(), SMLSPR_WIDTH, SMLSPR_HEIGHT);
				break;
			case BIG:
				group = BIG_REG_GRP;
				setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
				break;
			case FIRE:
				group = BIG_FIRE_GRP;
				setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
				break;
		}
		if(isStarPowered) {
			group = getStarPowerFrameGroup(parentPowerState);
			starPowerTimer += delta;
		}

		// choose correct size anim category
		Animation<TextureRegion>[][] sizeAnim;
		if(parentPowerState.isBigBody() && !parentMoveState.isDead())
			sizeAnim = bigAnim;
		else
			sizeAnim = smlAnim;

		float timer = parentMoveStateTimer;
		int pose = STAND_POSE;
		Vector2 offset = new Vector2(0f, 0f);
		if(didShootFireball)
			throwPoseCooldown = THROW_POSE_TIME;
		// check for fireball pose
		if(throwPoseCooldown > 0f && parentPowerState.isBigBody()) {
			pose = THROW_POSE;
			if(parentMoveState.isDuck())
				offset.set(SPRITE_DUCK_OFFSET);
		}
		// other poses...
		else {
			switch(parentMoveState) {
				case STAND:
				case FALL:
					pose = STAND_POSE;
					break;
				case DUCKSLIDE:
					pose = STAND_POSE;
					offset.set(SPRITE_DUCK_OFFSET);
					break;
				case RUN:
					pose = RUN_POSE;
					break;
				case BRAKE:
					pose = BRAKE_POSE;
					break;
				case DUCK:
				case DUCKFALL:
				case DUCKJUMP:
					pose = DUCK_POSE;
					offset.set(SPRITE_DUCK_OFFSET);
					break;
				case JUMP:
					pose = JUMP_POSE;
					break;
				case DEAD:
				case DEAD_BOUNCE:
					pose = DEAD_POSE;
					group = SML_REG_GRP;
					setBounds(getX(), getY(), SMLSPR_WIDTH, SMLSPR_HEIGHT);
					break;
				case CLIMB:
					pose = CLIMB_POSE;
					// if this is first frame of climb animation then reset clim anim timer
					if(prevParentMoveState != MoveState.CLIMB)
						climbAnimTimer = 0f;
					// if climbing up then forward the animation
					if(moveDir == Direction4.UP)
						climbAnimTimer += delta;
					// if climbing down then reverse the animation
					else if(moveDir == Direction4.DOWN) {
						climbAnimTimer = CommonInfo.ensurePositive(climbAnimTimer - delta,
								sizeAnim[pose][group].getAnimationDuration());
					}
					timer = climbAnimTimer;
					break;
			}
		}

		// reduce throw pose cooldown
		throwPoseCooldown = throwPoseCooldown < delta ? 0f : throwPoseCooldown-delta;

		setRegion(sizeAnim[pose][group].getKeyFrame(timer));
		setPosition(position.x - getWidth()/2f + offset.x, position.y - getHeight()/2f + offset.y);
	}

	private int getStarPowerFrameGroup(PowerState powerState) {
		switch(Math.floorMod((int) (starPowerTimer / STARPOWER_ANIM_SPEED), NUM_STARPOWER_FRAMES)) {
			case 3:
				switch(powerState) {
					case FIRE:
						return BIG_FIRE_GRP;
					case BIG:
						return BIG_REG_GRP;
					case SMALL:
						return SML_REG_GRP;
				}
			case 2:
				if(powerState.isBigBody())
					return BIG_INV3_GRP;
				else
					return SML_INV3_GRP;
			case 1:
				if(powerState.isBigBody())
					return BIG_INV2_GRP;
				else
					return SML_INV2_GRP;
			case 0:
			default:
				if(powerState.isBigBody())
					return BIG_INV1_GRP;
				else
					return SML_INV1_GRP;
		}
	}

	private void processGrowState(Vector2 position) {
		setRegion(bigAnim[GROW_POSE][BIG_REG_GRP].getKeyFrame(spriteStateTimer));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	private void processShrinkState(Vector2 position) {
		setRegion(bigAnim[SHRINK_POSE][BIG_REG_GRP].getKeyFrame(spriteStateTimer));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f + SHRINK_OFFSET_Y);
	}

	@Override
	public void draw(Batch batch) {
		if(isDrawAllowed)
			super.draw(batch);
	}
}
