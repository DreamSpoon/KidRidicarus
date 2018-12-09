package kidridicarus.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.GameInfo;

public class StaticCoinSprite extends Sprite {
	private static final float ANIM_SPEED = 0.15f;

	private Animation<TextureRegion> coinAnimation;
	private float stateTimer;

	public StaticCoinSprite(TextureAtlas atlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_STATIC), 0, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_STATIC), 0, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_STATIC), 0, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_STATIC), 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_STATIC), 32, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_STATIC), 16, 0, 16, 16));
		coinAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		setRegion(coinAnimation.getKeyFrame(0));
		setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		stateTimer = 0f;
	}

	public void update(float delta) {
		setRegion(coinAnimation.getKeyFrame(stateTimer, true));
		stateTimer += delta;
	}
}

