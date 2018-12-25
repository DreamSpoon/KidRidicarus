package kidridicarus.agent.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class BounceCoinSprite extends Sprite {
	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 16;
	private static final float ANIM_SPEED = 1f / 30f;

	private Animation<TextureRegion> spinAnimation;
	private float stateTimer;

	public BounceCoinSprite(TextureAtlas atlas, Vector2 position) {
		spinAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.General.COIN_SPIN), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		setBounds(getX(), getY(), UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		stateTimer += delta;
	}
}
