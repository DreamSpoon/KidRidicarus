package kidridicarus.guide.hud;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Array;

import kidridicarus.info.GameInfo;
import kidridicarus.tools.EncapTexAtlas;

public class CoinHudDrawable extends BaseDrawable {
	private static final float ANIM_SPEED = 0.2f;

	private Animation<TextureRegion> coinAnimation;
	private float stateTimer;

	public CoinHudDrawable(EncapTexAtlas encapTexAtlas) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_COIN_HUD), 0, 0, 8, 8));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_COIN_HUD), 0, 0, 8, 8));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_COIN_HUD), 8, 0, 8, 8));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_COIN_HUD), 16, 0, 8, 8));
		frames.add(new TextureRegion(encapTexAtlas.findRegion(GameInfo.TEXATLAS_COIN_HUD), 8, 0, 8, 8));
		coinAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);
		setMinWidth(8);
		setMinHeight(8);
		stateTimer = 0f;
	}

	public void act(float delta) {
		stateTimer += delta;
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		batch.draw(coinAnimation.getKeyFrame(stateTimer, true), x, y, width, height);
	}
}
