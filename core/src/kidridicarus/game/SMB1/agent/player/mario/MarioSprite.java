package kidridicarus.game.SMB1.agent.player.mario;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.SMB1.agent.player.mario.MarioBrain.MoveState;
import kidridicarus.game.SMB1.agent.player.mario.MarioBrain.PowerState;
import kidridicarus.game.info.SMB1_Gfx;

class MarioSprite extends AgentSprite {
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

	private MoveState parentPrevMoveState;
	private PowerState parentPrevPowerState;
	private float parentMoveStateTimer;
	private float throwPoseCooldown;
	private boolean isDrawAllowed;

	private enum SpriteState { NORMAL, GROW, SHRINK }
	private SpriteState spriteState;
	private float spriteStateTimer;
	private float starPowerTimer;
	private float climbAnimTimer;

	MarioSprite(TextureAtlas atlas, Vector2 position, PowerState parentPowerState, boolean isFacingRight) {
		createAnimations(atlas);
		parentPrevMoveState = MoveState.STAND;
		parentPrevPowerState = parentPowerState;
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
		postFrameInput(SprFrameTool.placeFaceR(position, isFacingRight));
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

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;

		MarioSpriteFrameInput myFrameInput = (MarioSpriteFrameInput) frameInput;
		SpriteFrameInput frameOut = frameInput.cpy();

		SpriteState nextSpriteState = getNextSpriteState(myFrameInput.powerState);
		boolean spriteStateChanged = nextSpriteState != spriteState;
		switch(nextSpriteState) {
			case NORMAL:
				frameOut = processPowerState(myFrameInput);
				break;
			case GROW:
				if(spriteStateChanged)
					setBounds(getX(), getY(), BIGSPR_WIDTH, BIGSPR_HEIGHT);
				processGrowState(frameInput.position);
				break;
			case SHRINK:
				processShrinkState(frameInput.position);
				break;
		}

		// if blinking due to damage invulnerability, then flicker the sprite
		if(myFrameInput.isDmgFrame && isDrawAllowed) {
			isDrawAllowed = false;
			frameOut = null;
		}
		else
			isDrawAllowed = true;

		parentPrevPowerState = myFrameInput.powerState;

		parentMoveStateTimer = myFrameInput.moveState == parentPrevMoveState ?
				parentMoveStateTimer+frameInput.frameTime.timeDelta : 0f;
		parentPrevMoveState = myFrameInput.moveState;

		spriteStateTimer = nextSpriteState == spriteState ? spriteStateTimer+frameInput.frameTime.timeDelta : 0f;
		spriteState = nextSpriteState;

		postFrameInput(frameOut);
	}

	private SpriteState getNextSpriteState(PowerState parentPowerState) {
		// did parent grow? if so then do grow anim
		if(parentPowerState.isBigBody() && !parentPrevPowerState.isBigBody())
			return SpriteState.GROW;
		// did parent shrink? if so then do shrink anim
		else if(!parentPowerState.isBigBody() && parentPrevPowerState.isBigBody())
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

	private SpriteFrameInput processPowerState(MarioSpriteFrameInput frameInput) {
		SpriteFrameInput frameOut = frameInput.cpy();
		int group = SML_REG_GRP;

		switch(frameInput.powerState) {
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
		if(frameInput.isStarPowered) {
			group = getStarPowerFrameGroup(frameInput.powerState);
			starPowerTimer += frameInput.frameTime.timeDelta;
		}

		// choose correct size anim category
		Animation<TextureRegion>[][] sizeAnim;
		if(frameInput.powerState.isBigBody() && !frameInput.moveState.isDead())
			sizeAnim = bigAnim;
		else
			sizeAnim = smlAnim;

		float timer = parentMoveStateTimer;
		int pose = STAND_POSE;
		if(frameInput.didShootFireball)
			throwPoseCooldown = THROW_POSE_TIME;
		// check for fireball pose
		if(!frameInput.moveState.isDead() && throwPoseCooldown > 0f && frameInput.powerState.isBigBody()) {
			pose = THROW_POSE;
			if(frameInput.moveState.isDuck())
				frameOut.position.add(SPRITE_DUCK_OFFSET);
		}
		// other poses...
		else {
			switch(frameInput.moveState) {
				case STAND:
				case FALL:
					pose = STAND_POSE;
					break;
				case DUCKSLIDE:
					pose = STAND_POSE;
					frameOut.position.add(SPRITE_DUCK_OFFSET);
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
					frameOut.position.add(SPRITE_DUCK_OFFSET);
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
					if(parentPrevMoveState != MoveState.CLIMB)
						climbAnimTimer = 0f;
					// if climbing up then forward the animation
					if(frameInput.climbDir == Direction4.UP)
						climbAnimTimer += frameInput.frameTime.timeDelta;
					// if climbing down then reverse the animation
					else if(frameInput.climbDir == Direction4.DOWN) {
						climbAnimTimer = CommonInfo.ensurePositive(climbAnimTimer - frameInput.frameTime.timeDelta,
								sizeAnim[pose][group].getAnimationDuration());
					}
					timer = climbAnimTimer;
					break;
			}
		}

		// reduce throw pose cooldown
		throwPoseCooldown = throwPoseCooldown < frameInput.frameTime.timeDelta ?
				0f : throwPoseCooldown-frameInput.frameTime.timeDelta;
		setRegion(sizeAnim[pose][group].getKeyFrame(timer));
		return frameOut;
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
}
