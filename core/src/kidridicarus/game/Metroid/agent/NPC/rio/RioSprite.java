package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.MetroidGfx;

public class RioSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(24);
	private static final float SPRITE_HEIGHT = UInfo.P2M(24);
	private static final float ANIM_SPEED_FLAP = 2/15f;
	private static final float ANIM_SPEED_SWOOP = 1/20f;

	// regular and fast spinning animations
	private Animation<TextureRegion> flapAnim;
	private Animation<TextureRegion> swoopAnim;
	private Animation<TextureRegion> injuryAnim;

	private float stateTimer;

	public RioSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		flapAnim = new Animation<TextureRegion>(ANIM_SPEED_FLAP,
				atlas.findRegions(MetroidGfx.NPC.RIO), PlayMode.LOOP);
		swoopAnim = new Animation<TextureRegion>(ANIM_SPEED_SWOOP,
				atlas.findRegions(MetroidGfx.NPC.RIO),PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED_FLAP,
				atlas.findRegions(MetroidGfx.NPC.RIO_HIT), PlayMode.LOOP);
		stateTimer = 0;
		setRegion(flapAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		isVisible = frameInput.visible;
		switch(((RioSpriteFrameInput) frameInput).moveState) {
			case FLAP:
			default:
				setRegion(flapAnim.getKeyFrame(stateTimer));
				break;
			case SWOOP:
				setRegion(swoopAnim.getKeyFrame(stateTimer));
				break;
			case INJURY:
				// do not animate injury, use one frame only - TODO check this
				setRegion(injuryAnim.getKeyFrame(0f));
				break;
			case DEAD:
				break;
		}
		if((frameInput.flipX && !isFlipX()) || (!frameInput.flipX && isFlipX()))
			flip(true,  false);
		setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
		stateTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
	}
}
