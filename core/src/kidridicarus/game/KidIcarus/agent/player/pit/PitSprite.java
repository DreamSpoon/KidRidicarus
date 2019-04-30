package kidridicarus.game.KidIcarus.agent.player.pit;

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
import kidridicarus.game.KidIcarus.agent.player.pit.Pit.MoveState;
import kidridicarus.game.info.KidIcarusGfx;

public class PitSprite extends AgentSprite {
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(24);
	private static final Vector2 BIG_SPRITE_OFFSET = UInfo.VectorP2M(0, 1);
	private static final Vector2 BIG_SPRITE_STUCK_OFFSET = UInfo.VectorP2M(0, 5);
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final Vector2 SML_SPRITE_OFFSET = UInfo.VectorP2M(0, 1);
	private static final float ANIM_SPEED = 1/15f;
	private static final float DMG_ANIM_SPEED = 1/60f;
	private static final int ANIM_HOLD = 0;
	private static final int ANIM_SHOOT = 1;
	private static final int ANIM_GRP_REG = 0;

	private Animation<TextureRegion>[][] aimUpAnim;
	private Animation<TextureRegion>[][] jumpAnim;
	private Animation<TextureRegion>[][] standAnim;
	private Animation<TextureRegion>[][] walkAnim;
	private Animation<TextureRegion>[] climbAnim;
	private Animation<TextureRegion>[] duckAnim;
	private Animation<TextureRegion> deadAnim;
	private MoveState parentPrevState;
	private float generalAnimTimer;
	private float climbAnimTimer;
	private float dmgFrameTimer;

	@SuppressWarnings("unchecked")
	public PitSprite(TextureAtlas atlas, Vector2 position) {
		aimUpAnim = new Animation[KidIcarusGfx.Player.Pit.GRPDIR.length][2];
		jumpAnim = new Animation[KidIcarusGfx.Player.Pit.GRPDIR.length][2];
		standAnim = new Animation[KidIcarusGfx.Player.Pit.GRPDIR.length][2];
		walkAnim = new Animation[KidIcarusGfx.Player.Pit.GRPDIR.length][2];
		climbAnim = new Animation[KidIcarusGfx.Player.Pit.GRPDIR.length];
		duckAnim = new Animation[KidIcarusGfx.Player.Pit.GRPDIR.length];

		// PlayMode.NORMAL with shoot animations so that Pit shows arrow empty until isShooting = false in
		// the update() method. Regular animations use PlayMode.LOOP, e.g. so that walking animation repeats.
		for(int i=0; i<KidIcarusGfx.Player.Pit.GRPDIR.length; i++) {
			aimUpAnim[i][ANIM_HOLD] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.AIMUP), PlayMode.NORMAL);
			aimUpAnim[i][ANIM_SHOOT] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.AIMUP_SHOOT), PlayMode.NORMAL);
			jumpAnim[i][ANIM_HOLD] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.JUMP), PlayMode.NORMAL);
			jumpAnim[i][ANIM_SHOOT] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.JUMP_SHOOT), PlayMode.NORMAL);
			standAnim[i][ANIM_HOLD] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.STAND), PlayMode.NORMAL);
			standAnim[i][ANIM_SHOOT] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.STAND_SHOOT), PlayMode.NORMAL);
			walkAnim[i][ANIM_HOLD] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.WALK), PlayMode.LOOP);
			walkAnim[i][ANIM_SHOOT] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.WALK_SHOOT), PlayMode.LOOP);

			climbAnim[i] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.CLIMB), PlayMode.LOOP);
			duckAnim[i] = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
					KidIcarusGfx.Player.Pit.GRPDIR[i]+KidIcarusGfx.Player.Pit.DUCK), PlayMode.NORMAL);
		}
		deadAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(
				KidIcarusGfx.Player.Pit.GRPDIR[ANIM_GRP_REG]+KidIcarusGfx.Player.Pit.DEAD), PlayMode.NORMAL);

		parentPrevState = null;
		generalAnimTimer = 0f;
		climbAnimTimer = 0f;
		dmgFrameTimer = -1f;

		setRegion(standAnim[ANIM_GRP_REG][ANIM_HOLD].getKeyFrame(0f));
		setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		postFrameInput(new SpriteFrameInput(position));
	}

//	public void update(float delta, Vector2 position, MoveState nextParentState, boolean isFacingRight,
//			boolean isDmgFrame, boolean isShooting, boolean isHeadInTile, boolean isJumpUp, Direction4 climbDir) {
	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput.visible))
			return;

		PitSpriteFrameInput myFrameInput = (PitSpriteFrameInput) frameInput;
		if(!myFrameInput.isDmgFrame)
			dmgFrameTimer = -1f;
		else
			dmgFrameTimer = dmgFrameTimer == -1 ? 0f : dmgFrameTimer+myFrameInput.timeDelta;

		Animation<TextureRegion> nextAnim = null;
		boolean isBigSprite = true;
		switch(myFrameInput.moveState) {
			case AIMUP:
			case PRE_JUMP_AIMUP:
			case JUMP_AIMUP:
				if(myFrameInput.isShooting)
					nextAnim = aimUpAnim[getGrpForDmgTimer()][ANIM_SHOOT];
				else
					nextAnim = aimUpAnim[getGrpForDmgTimer()][ANIM_HOLD];
				break;
			case DUCK:
			case PRE_JUMP_DUCK:
			case JUMP_DUCK:
				nextAnim = duckAnim[getGrpForDmgTimer()];
				isBigSprite = false;
				break;
			case PRE_JUMP:
			case JUMP:
				// if parent is in the "move up" phase of jump then show jump animation 
				if(myFrameInput.isJumpUp) {
					if(myFrameInput.isShooting)
						nextAnim = jumpAnim[getGrpForDmgTimer()][ANIM_SHOOT];
					else
						nextAnim = jumpAnim[getGrpForDmgTimer()][ANIM_HOLD];
				}
				// otherwise show first frame of jump animation
				else {
					if(myFrameInput.isShooting) 
						setRegion(jumpAnim[getGrpForDmgTimer()][ANIM_SHOOT].getKeyFrame(0f));
					else
						setRegion(jumpAnim[getGrpForDmgTimer()][ANIM_HOLD].getKeyFrame(0f));
				}
				break;
			case STAND:
				if(myFrameInput.isShooting)
					nextAnim = standAnim[getGrpForDmgTimer()][ANIM_SHOOT];
				else
					nextAnim = standAnim[getGrpForDmgTimer()][ANIM_HOLD];
				break;
			case WALK:
				if(myFrameInput.isShooting)
					nextAnim = walkAnim[getGrpForDmgTimer()][ANIM_SHOOT];
				else
					nextAnim = walkAnim[getGrpForDmgTimer()][ANIM_HOLD];
				break;
			case CLIMB:
				// if this is first frame of climb animation then reset climb anim timer
				if(parentPrevState != MoveState.CLIMB)
					climbAnimTimer = 0f;
				// if climbing up then forward the animation
				if(myFrameInput.climbDir == Direction4.UP)
					climbAnimTimer += myFrameInput.timeDelta;
				// if climbing down then reverse the animation
				else if(myFrameInput.climbDir == Direction4.DOWN) {
					climbAnimTimer = CommonInfo.ensurePositive(climbAnimTimer - myFrameInput.timeDelta,
							climbAnim[getGrpForDmgTimer()].getAnimationDuration());
				}
				setRegion(climbAnim[getGrpForDmgTimer()].getKeyFrame(climbAnimTimer));
				break;
			case DEAD:
				nextAnim = deadAnim;
				break;
		}
		// nextAnim will equal null here if doing the climb animation
		if(nextAnim != null)
			setRegion(nextAnim.getKeyFrame(generalAnimTimer));
		// check sprite size, set bounds and position
		Vector2 offset = new Vector2(0f, 0f);
		if(isBigSprite) {
			if(myFrameInput.isHeadInTile)
				offset.set(BIG_SPRITE_STUCK_OFFSET);
			else
				offset.set(BIG_SPRITE_OFFSET);
			setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		}
		else {
			offset.set(SML_SPRITE_OFFSET);
			setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
		}

		if(parentPrevState != null && parentPrevState.isJump() && myFrameInput.moveState.isJump())
			generalAnimTimer += myFrameInput.timeDelta;
		else
			generalAnimTimer = parentPrevState == myFrameInput.moveState ? generalAnimTimer+myFrameInput.timeDelta : 0f;
		parentPrevState = myFrameInput.moveState;

		postFrameInput(new SpriteFrameInput(myFrameInput.position.cpy().add(offset), !myFrameInput.flipX));
	}

	private int getGrpForDmgTimer() {
		if(dmgFrameTimer == -1f)
			return ANIM_GRP_REG;
		else
			return Math.floorMod((int) (dmgFrameTimer / DMG_ANIM_SPEED), KidIcarusGfx.Player.Pit.GRPDIR.length);
	}
}
