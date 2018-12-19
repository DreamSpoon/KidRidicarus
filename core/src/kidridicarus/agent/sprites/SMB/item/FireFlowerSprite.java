package kidridicarus.agent.sprites.SMB.item;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class FireFlowerSprite extends Sprite {
	private static final float ANIM_SPEED = 0.2f;

	private Animation<TextureRegion> flowerAnimation;
	private float stateTimer;

	public FireFlowerSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREFLOWER, 0, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREFLOWER, 16, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREFLOWER, 32, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FIREFLOWER, 48, 0, 16, 16));
		flowerAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = 0f;
		setRegion(flowerAnimation.getKeyFrame(0));
		setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setRegion(flowerAnimation.getKeyFrame(stateTimer, true));

		// update sprite position
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);

		stateTimer += delta;
	}
}
