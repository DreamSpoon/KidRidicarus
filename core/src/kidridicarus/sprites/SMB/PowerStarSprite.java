package kidridicarus.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class PowerStarSprite extends Sprite {
	private static final float ANIM_SPEED = 0.075f;

	private Animation<TextureRegion> starAnimation;
	private float stateTimer;

	public PowerStarSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		super(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 0, 0, 16, 16));
		setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 0, 0, 16, 16));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 16, 0, 16, 16));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 32, 0, 16, 16));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 48, 0, 16, 16));
		starAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = 0f;
	}

	public void update(float delta, Vector2 position) {
		setRegion(starAnimation.getKeyFrame(stateTimer, true));
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
		stateTimer += delta;
	}
}
