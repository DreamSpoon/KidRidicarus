package kidridicarus.game.SMB1.agent.NPC.turtle;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMB1_Gfx;

public class TurtleSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(24);
	private static final float ANIM_SPEED = 0.25f;

	private Animation<TextureRegion> walkAnim;
	private TextureRegion insideShell;
	private Animation<TextureRegion> wakeUpAnim;
	private float stateTimer;

	public TurtleSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.NPC.TURTLE_WALK), PlayMode.LOOP);
		wakeUpAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.NPC.TURTLE_WAKEUP), PlayMode.LOOP);
		insideShell = atlas.findRegion(SMB1_Gfx.NPC.TURTLE_HIDE);
		stateTimer = 0;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		isVisible = frameInput.visible;
		switch(((TurtleSpriteFrameInput) frameInput).moveState) {
			case WALK:
			case FALL:
				setRegion(walkAnim.getKeyFrame(stateTimer));
				break;
			case HIDE:
			case SLIDE:
				setRegion(insideShell);
				break;
			case WAKE:
				setRegion(wakeUpAnim.getKeyFrame(stateTimer));
				break;
			case DEAD:
				setRegion(insideShell);
				// upside down when dead
				if(!isFlipY())
					flip(false,  true);
				break;
		}
		if((frameInput.flipX && !isFlipX()) || (!frameInput.flipX && isFlipX()))
			flip(true,  false);
		setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
		stateTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
	}
}
