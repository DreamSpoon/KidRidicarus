package kidridicarus.game.Metroid.agent.player.samus;

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
import kidridicarus.game.Metroid.agent.player.samus.SamusBrain.MoveState;
import kidridicarus.game.info.MetroidGfx;

class SamusSprite extends AgentSprite {
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(32);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(40);
	private static final Vector2 BIG_SPRITE_OFFSET = UInfo.VectorP2M(0, 5);
	private static final float MED_SPRITE_WIDTH = UInfo.P2M(24);
	private static final float MED_SPRITE_HEIGHT = UInfo.P2M(24);
	private static final Vector2 MED_SPRITE_OFFSET = UInfo.VectorP2M(0, 5);
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final Vector2 SML_SPRITE_OFFSET = UInfo.VectorP2M(0, 0);
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

	SamusSprite(TextureAtlas atlas, Vector2 position) {
		aimRightAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.AIMRIGHT), PlayMode.LOOP);
		aimUpAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.AIMUP), PlayMode.LOOP);
		runAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.RUN), PlayMode.LOOP);
		runAimRightAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.RUN_AIMRIGHT), PlayMode.LOOP);
		runAimUpAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.RUN_AIMUP), PlayMode.LOOP);
		jumpAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.JUMP), PlayMode.LOOP);
		jumpAimRightAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.JUMP_AIMRIGHT), PlayMode.LOOP);
		jumpAimUpAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.JUMP_AIMUP), PlayMode.LOOP);
		jumpSpinAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.JUMPSPIN), PlayMode.LOOP);
		ballAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.BALL), PlayMode.LOOP);
		climbAnim = new Animation<TextureRegion>
				(ANIM_SPEED, atlas.findRegions(MetroidGfx.Player.Samus.CLIMB), PlayMode.LOOP);
		stateTimer = 0f;
		curParentState = null;
		climbAnimTimer = 0f;
		isDrawAllowed = true;
		setRegion(aimRightAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;

		SamusSpriteFrameInput myFrameInput = (SamusSpriteFrameInput) frameInput;
		SpriteFrameInput frameOut = frameInput.cpy();
		switch(myFrameInput.moveState) {
			case STAND:
				if(myFrameInput.isFacingUp)
					setRegion(aimUpAnim.getKeyFrame(stateTimer));
				else
					setRegion(aimRightAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				frameOut.position.add(BIG_SPRITE_OFFSET);
				break;
			case RUN:
			case RUNSHOOT:
				if(myFrameInput.isFacingUp)
					setRegion(runAimUpAnim.getKeyFrame(stateTimer));
				else {
					if(myFrameInput.moveState == MoveState.RUN)
						setRegion(runAnim.getKeyFrame(stateTimer));
					else
						setRegion(runAimRightAnim.getKeyFrame(stateTimer));
				}
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				frameOut.position.add(BIG_SPRITE_OFFSET);
				break;
			case PRE_JUMP:
			case PRE_JUMPSHOOT:
			case PRE_JUMPSPIN:
			case JUMP:
				if(myFrameInput.isFacingUp)
					setRegion(jumpAimUpAnim.getKeyFrame(stateTimer));
				else
					setRegion(jumpAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				frameOut.position.add(BIG_SPRITE_OFFSET);
				break;
			case JUMPSPIN:
				setRegion(jumpSpinAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				frameOut.position.add(MED_SPRITE_OFFSET);
				break;
			case JUMPSHOOT:
			case JUMPSPINSHOOT:
				if(myFrameInput.isFacingUp)
					setRegion(jumpAimUpAnim.getKeyFrame(stateTimer));
				else
					setRegion(jumpAimRightAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				frameOut.position.add(BIG_SPRITE_OFFSET);
				break;
			case BALL_GRND:
			case BALL_AIR:
				setRegion(ballAnim.getKeyFrame(stateTimer));
				setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
				frameOut.position.add(SML_SPRITE_OFFSET);
				break;
			case CLIMB:
				// if this is first frame of climb animation then reset clim anim timer
				if(curParentState != MoveState.CLIMB)
					climbAnimTimer = 0f;
				// if climbing up then forward the animation
				if(myFrameInput.climbDir == Direction4.UP)
					climbAnimTimer += frameInput.frameTime.timeDelta;
				// if climbing down then reverse the animation
				else if(myFrameInput.climbDir == Direction4.DOWN) {
					climbAnimTimer = CommonInfo.ensurePositive(climbAnimTimer - frameInput.frameTime.timeDelta,
							climbAnim.getAnimationDuration());
				}
				setRegion(climbAnim.getKeyFrame(climbAnimTimer));
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				frameOut.position.add(MED_SPRITE_OFFSET);
				break;
			// samus is not drawn during dead anim state
			case DEAD:
				frameOut = null;
				break;
		}

		stateTimer = curParentState == myFrameInput.moveState ? stateTimer+frameInput.frameTime.timeDelta : 0f;
		curParentState = myFrameInput.moveState;

		// flicker sprite during damage frames
		if(myFrameInput.isDmgFrame && isDrawAllowed) {
			isDrawAllowed = false;
			frameOut = null;
		}
		else
			isDrawAllowed = true;

		postFrameInput(frameOut);
	}
}
