package kidridicarus.guide.hud;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import kidridicarus.info.SMBAnim;

public class CoinHudDrawable extends BaseDrawable {
	private static final float ANIM_SPEED = 0.2f;

	private Animation<TextureRegion> coinAnimation;
	private float stateTimer;

	/*
	 * Source: https://stackoverflow.com/questions/6667243/using-enum-values-as-string-literals
	 * Date: 2018.12.25
	 */
	public CoinHudDrawable(TextureAtlas atlas) {
		coinAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.General.COIN), PlayMode.LOOP);
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
