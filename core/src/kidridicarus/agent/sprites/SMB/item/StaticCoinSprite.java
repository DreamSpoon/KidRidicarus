package kidridicarus.agent.sprites.SMB.item;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class StaticCoinSprite extends Sprite {
	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 16;
	private static final float ANIM_SPEED = 0.15f;

	private Animation<TextureRegion> coinAnimation;
	private float stateTimer;

	public StaticCoinSprite(TextureAtlas atlas, Vector2 position) {
		coinAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.Item.COIN_STATIC), PlayMode.LOOP);

		setRegion(coinAnimation.getKeyFrame(0));
		setBounds(getX(), getY(), UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer = 0f;
	}

	public void update(float delta) {
		setRegion(coinAnimation.getKeyFrame(stateTimer, true));
		stateTimer += delta;
	}
}

