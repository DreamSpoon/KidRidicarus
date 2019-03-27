package kidridicarus.game.agent.SMB.player.mario.HUD;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import kidridicarus.game.info.SMB_Gfx;

public class CoinHudDrawable extends BaseDrawable {
	private static final float ANIM_SPEED = 0.133f;

	private Animation<TextureRegion> coinAnim;
	private float globalTimer;

	public CoinHudDrawable(TextureAtlas atlas) {
		coinAnim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(SMB_Gfx.General.HUD_COIN),
				PlayMode.LOOP);
		setMinWidth(8);
		setMinHeight(8);
		globalTimer = 0f;
	}

	// the act function must be passed globalTimer instead of delta time
	public void act(float delta) {
		globalTimer = delta;
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		batch.draw(coinAnim.getKeyFrame(globalTimer, true), x, y, width, height);
	}
}
