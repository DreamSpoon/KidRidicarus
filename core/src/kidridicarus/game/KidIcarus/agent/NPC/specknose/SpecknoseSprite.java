package kidridicarus.game.KidIcarus.agent.NPC.specknose;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agent.proactoragent.ProactorAgentSprite;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class SpecknoseSprite extends ProactorAgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2/15f;

	private Animation<TextureRegion> flyAnim;
	private float stateTimer;

	public SpecknoseSprite(TextureAtlas atlas, Vector2 position) {
		flyAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(KidIcarusGfx.NPC.SPECKNOSE), PlayMode.LOOP);
		stateTimer = 0;
		setRegion(flyAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		isVisible = frameInput.visible;
		setRegion(flyAnim.getKeyFrame(stateTimer));
		if((frameInput.flipX && !isFlipX()) || (!frameInput.flipX && isFlipX()))
			flip(true,  false);
		setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
		stateTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
	}
}
