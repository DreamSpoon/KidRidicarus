package kidridicarus.game.agent.SMB.player.mario.HUD;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class HudCoin extends Image {
	private CoinHudDrawable drawable;
	private float stateTimer;

	public HudCoin(TextureAtlas atlas) {
		super();
		drawable = new CoinHudDrawable(atlas);
		setDrawable(drawable);
		stateTimer = 0f;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		drawable.act(stateTimer);
		stateTimer += delta;
	}
}
