package kidridicarus.game.SMB.agentsprite.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB.agent.player.Luigi.MoveState;
import kidridicarus.game.SMB.agent.player.Luigi.PowerState;

public class LuigiSprite extends Sprite {
	private static final float SMLSPR_WIDTH = UInfo.P2M(16);
	private static final float SMLSPR_HEIGHT = UInfo.P2M(16);
	private static final float BIGSPR_WIDTH = UInfo.P2M(16);
	private static final float BIGSPR_HEIGHT = UInfo.P2M(32);
//	private static final float STARPOWER_ANIM_SPEED = 0.05f;
//	private static final int NUM_STARPOWER_FRAMES = 4;
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
//	private static final int BIG_INV1_GRP = 1;
//	private static final int BIG_INV2_GRP = 2;
//	private static final int BIG_INV3_GRP = 3;
	private static final int BIG_FIRE_GRP = 4;
	private static final int SML_NUM_GRPS = 4;
	private static final int SML_REG_GRP = 0;
//	private static final int SML_INV1_GRP = 1;
//	private static final int SML_INV2_GRP = 2;
//	private static final int SML_INV3_GRP = 3;

	/*
	 * Animations by body size and [pose][group], where:
	 *   pose is stuff like "stand", "run", "jump", etc.
	 *   group is stuff like "small regular (red suit)", "big regular (red suit)", "big fire (white suit)",
	 *     "small star powered (shimmering suit)", etc.
	 */
	private Animation<TextureRegion>[][] smlAnim;
	private Animation<TextureRegion>[][] bigAnim;

	private MoveState prevParentMoveState;
	private float stateTimer;

	public LuigiSprite(TextureAtlas atlas, Vector2 position, PowerState parentPowerState, boolean facingRight) {
		createAnimations(atlas);

		prevParentMoveState = MoveState.STAND;
		stateTimer = 0f;

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

	public void update(float delta, Vector2 position, MoveState parentMoveState, PowerState parentPowerState,
			boolean facingRight) {
		int group;
		switch(parentPowerState) {
			case SMALL:
			default:
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
		int pose;
		switch(parentMoveState) {
			case STAND:
			default:
				pose = STAND_POSE;
				break;
			case RUN:
				pose = RUN_POSE;
				break;
		}

		if(parentPowerState.isBigBody())
			setRegion(bigAnim[pose][group].getKeyFrame(stateTimer));
		else
			setRegion(smlAnim[pose][group].getKeyFrame(stateTimer));
		// flip to face left if necessary
		if(!facingRight && !isFlipX())
			flip(true, false);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer = parentMoveState == prevParentMoveState ? stateTimer+delta : 0f;
		prevParentMoveState = parentMoveState;
	}
}
