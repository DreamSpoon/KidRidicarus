package kidridicarus.game.agent.KidIcarus.player.pit;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.KidIcarus.player.pit.Pit.MoveState;
import kidridicarus.game.info.KidIcarusGfx;

public class PitSprite extends Sprite {
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(24);
	private static final Vector2 BIG_SPRITE_OFFSET = UInfo.P2MVector(0, 1);
	private static final Vector2 BIG_SPRITE_STUCK_OFFSET = UInfo.P2MVector(0, 5);

	private static final float SML_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final Vector2 SML_SPRITE_OFFSET = UInfo.P2MVector(0, 1);

	private static final float ANIM_SPEED = 1/15f;
	private static final int ANIM_HOLD = 0;
	private static final int ANIM_SHOOT = 1;

	private Animation<TextureRegion>[] aimUpAnim;
	private Animation<TextureRegion>[] jumpAnim;
	private Animation<TextureRegion>[] standAnim;
	private Animation<TextureRegion>[] walkAnim;

	private Animation<TextureRegion> climbAnim;
	private Animation<TextureRegion> duckAnim;

	private Animation<TextureRegion> deadAnim;

	private MoveState prevParentState;
	private float spriteStateTimer;
	private float climbAnimTimer;

	@SuppressWarnings("unchecked")
	public PitSprite(TextureAtlas atlas, Vector2 position) {
		aimUpAnim = new Animation[2];
		jumpAnim = new Animation[2];
		standAnim = new Animation[2];
		walkAnim = new Animation[2];

		// PlayMode.NORMAL with shoot animations so that Pit shows arrow empty until isShooting = false in
		// the update() method. Regular animations use PlayMode.LOOP, e.g. so that walking animation repeats.
		aimUpAnim[ANIM_HOLD] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.AIMUP), PlayMode.LOOP);
		aimUpAnim[ANIM_SHOOT] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.AIMUP_SHOOT), PlayMode.NORMAL);
		jumpAnim[ANIM_HOLD] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.JUMP), PlayMode.NORMAL);
		jumpAnim[ANIM_SHOOT] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.JUMP_SHOOT), PlayMode.NORMAL);
		standAnim[ANIM_HOLD] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.STAND), PlayMode.LOOP);
		standAnim[ANIM_SHOOT] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.STAND_SHOOT), PlayMode.NORMAL);
		walkAnim[ANIM_HOLD] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.WALK), PlayMode.LOOP);
		walkAnim[ANIM_SHOOT] = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.WALK_SHOOT), PlayMode.NORMAL);

		climbAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.CLIMB), PlayMode.LOOP);
		duckAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.DUCK), PlayMode.LOOP);

		deadAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.Player.Pit.DEAD), PlayMode.LOOP);

		prevParentState = null;
		spriteStateTimer = 0f;
		climbAnimTimer = 0f;

		setRegion(standAnim[0].getKeyFrame(0f));
		setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, MoveState nextParentState, boolean isFacingRight,
			boolean isDmgFrame, boolean isShooting, boolean isHeadInTile, boolean isJumpUp, Direction4 climbDir) {
		Animation<TextureRegion> nextAnim = null;
		boolean isBigSprite = true;
		switch(nextParentState) {
			case AIMUP:
			case PRE_JUMP_AIMUP:
			case JUMP_AIMUP:
				if(isShooting)
					nextAnim = aimUpAnim[ANIM_SHOOT];
				else
					nextAnim = aimUpAnim[ANIM_HOLD];
				break;
			case DUCK:
			case PRE_JUMP_DUCK:
			case JUMP_DUCK:
				nextAnim = duckAnim;
				isBigSprite = false;
				break;
			case PRE_JUMP:
			case JUMP:
				// if parent is in the "move up" phase of jump then show jump animation 
				if(isJumpUp) {
					if(isShooting)
						nextAnim = jumpAnim[ANIM_SHOOT];
					else
						nextAnim = jumpAnim[ANIM_HOLD];
				}
				// otherwise show first frame of jump animation
				else {
					if(isShooting) 
						setRegion(jumpAnim[ANIM_SHOOT].getKeyFrame(0f));
					else
						setRegion(jumpAnim[ANIM_HOLD].getKeyFrame(0f));
				}
				break;
			case STAND:
				if(isShooting)
					nextAnim = standAnim[ANIM_SHOOT];
				else
					nextAnim = standAnim[ANIM_HOLD];
				break;
			case WALK:
				if(isShooting)
					nextAnim = walkAnim[ANIM_SHOOT];
				else
					nextAnim = walkAnim[ANIM_HOLD];
				break;
			case CLIMB:
				// if this is first frame of climb animation then reset climb anim timer
				if(prevParentState != MoveState.CLIMB)
					climbAnimTimer = 0f;
				// if climbing up then forward the animation
				if(climbDir == Direction4.UP)
					climbAnimTimer += delta;
				// if climbing down then reverse the animation
				else if(climbDir == Direction4.DOWN) {
					climbAnimTimer = CommonInfo.ensurePositive(climbAnimTimer - delta,
							climbAnim.getAnimationDuration());
				}
				setRegion(climbAnim.getKeyFrame(climbAnimTimer));
				break;
			case DEAD:
				nextAnim = deadAnim;
				break;
		}

		// nextAnim will equal null here if doing the climb animation
		if(nextAnim != null)
			setRegion(nextAnim.getKeyFrame(spriteStateTimer));

		Vector2 offset = new Vector2(0f, 0f);
		// check sprite size, set bounds and position
		if(isBigSprite) {
			if(isHeadInTile)
				offset.set(BIG_SPRITE_STUCK_OFFSET);
			else
				offset.set(BIG_SPRITE_OFFSET);
			setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		}
		else {
			offset.set(SML_SPRITE_OFFSET);
			setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
		}
		setPosition(position.x - getWidth()/2f + offset.x,
				position.y - getHeight()/2f + offset.y);

		// should the sprite be flipped on X due to facing direction?
		if((isFacingRight && isFlipX()) || (!isFacingRight && !isFlipX()))
			flip(true,  false);

		if(prevParentState != null && prevParentState.isJump() && nextParentState.isJump())
			spriteStateTimer += delta;
		else
			spriteStateTimer = prevParentState == nextParentState ? spriteStateTimer+delta : 0f;
		prevParentState = nextParentState;
	}
}
