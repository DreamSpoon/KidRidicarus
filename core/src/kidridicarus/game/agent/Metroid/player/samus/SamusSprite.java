package kidridicarus.game.agent.Metroid.player.samus;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.Metroid.player.samus.Samus.MoveState;
import kidridicarus.game.info.MetroidAnim;

public class SamusSprite extends Sprite {
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(32);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(40);
	private static final Vector2 BIG_SPRITE_OFFSET = UInfo.P2MVector(0, 5);

	private static final float MED_SPRITE_WIDTH = UInfo.P2M(24);
	private static final float MED_SPRITE_HEIGHT = UInfo.P2M(24);
	private static final Vector2 MED_SPRITE_OFFSET = UInfo.P2MVector(0, 5);

	private static final float SML_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final Vector2 SML_SPRITE_OFFSET = UInfo.P2MVector(0, 0);

	private static final float ANIM_SPEED = 0.05f;

	private Animation<TextureRegion> aimRightAnim;
	private Animation<TextureRegion> aimUpAnim;
	private Animation<TextureRegion> runAnim;
	private Animation<TextureRegion> runAimRightAnim;
	private Animation<TextureRegion> runAimUpAnim;
	private Animation<TextureRegion> jumpAnim;
	private Animation<TextureRegion> jumpAimRightAnim;
	private Animation<TextureRegion> jumpAimUpAnim;
	private Animation<TextureRegion> jumpSpinAnim;
	private Animation<TextureRegion> ballAnim;
	private Animation<TextureRegion> climbAnim;
	private float climbAnimTimer;
	private MoveState curParentState;
	private float stateTimer;
	private boolean isDrawAllowed;

	public SamusSprite(TextureAtlas atlas, Vector2 position) {
		aimRightAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.AIMRIGHT), PlayMode.LOOP);
		aimUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.AIMUP), PlayMode.LOOP);
		runAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.RUN), PlayMode.LOOP);
		runAimRightAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.RUN_AIMRIGHT), PlayMode.LOOP);
		runAimUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.RUN_AIMUP), PlayMode.LOOP);
		jumpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMP), PlayMode.LOOP);
		jumpAimRightAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMP_AIMRIGHT), PlayMode.LOOP);
		jumpAimUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMP_AIMUP), PlayMode.LOOP);
		jumpSpinAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.JUMPSPIN), PlayMode.LOOP);
		ballAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.BALL), PlayMode.LOOP);
		climbAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.Player.CLIMB), PlayMode.LOOP);

		stateTimer = 0f;
		curParentState = null;
		climbAnimTimer = 0f;
		isDrawAllowed = true;

		setRegion(aimRightAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, MoveState nextParentState, boolean isFacingRight,
			boolean isFacingUp, boolean isBlinking, Direction4 climbDir) {
		Vector2 offset = new Vector2(0f, 0f);

		switch(nextParentState) {
			case STAND:
				if(isFacingUp)
					setRegion(aimUpAnim.getKeyFrame(stateTimer));
				else
					setRegion(aimRightAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case RUN:
			case RUNSHOOT:
				if(isFacingUp)
					setRegion(runAimUpAnim.getKeyFrame(stateTimer));
				else {
					if(nextParentState == MoveState.RUN)
						setRegion(runAnim.getKeyFrame(stateTimer));
					else
						setRegion(runAimRightAnim.getKeyFrame(stateTimer));
				}
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case PRE_JUMP:
			case PRE_JUMPSHOOT:
			case PRE_JUMPSPIN:
			case JUMP:
				if(isFacingUp)
					setRegion(jumpAimUpAnim.getKeyFrame(stateTimer));
				else
					setRegion(jumpAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case JUMPSPIN:
				setRegion(jumpSpinAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				offset.set(MED_SPRITE_OFFSET);
				break;
			case JUMPSHOOT:
			case JUMPSPINSHOOT:
				if(isFacingUp)
					setRegion(jumpAimUpAnim.getKeyFrame(stateTimer));
				else
					setRegion(jumpAimRightAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				offset.set(BIG_SPRITE_OFFSET);
				break;
			case BALL_GRND:
			case BALL_AIR:
				setRegion(ballAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
				offset.set(SML_SPRITE_OFFSET);
				break;
			case CLIMB:
				// if this is first frame of climb animation then reset clim anim timer
				if(curParentState != MoveState.CLIMB)
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
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				offset.set(MED_SPRITE_OFFSET);
				break;
		}

		// should the sprite be flipped on X due to facing direction?
		if((isFacingRight && isFlipX()) || (!isFacingRight && !isFlipX()))
			flip(true,  false);

		// update sprite position
		setPosition(position.x - getWidth()/2 + offset.x, position.y - getHeight()/2 + offset.y);

		if(isBlinking && isDrawAllowed)
			isDrawAllowed = false;
		else
			isDrawAllowed = true;

		stateTimer = curParentState == nextParentState ? stateTimer+delta : 0f;
		curParentState = nextParentState;
	}

	@Override
	public void draw(Batch batch) {
		if(isDrawAllowed)
			super.draw(batch);
	}
}
