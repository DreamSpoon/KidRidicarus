package kidridicarus.agent.sprites.SMB.item;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class FireFlowerSprite extends Sprite {
	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 16;
	private static final float ANIM_SPEED = 0.2f;

	private Animation<TextureRegion> flowerAnimation;
	private float stateTimer;

	public FireFlowerSprite(TextureAtlas atlas, Vector2 position) {
		flowerAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.Item.FIREFLOWER), PlayMode.LOOP);

		stateTimer = 0f;
		setRegion(flowerAnimation.getKeyFrame(0));
		setBounds(getX(), getY(), UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setRegion(flowerAnimation.getKeyFrame(stateTimer, true));

		// update sprite position
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);

		stateTimer += delta;
	}
}
