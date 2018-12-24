package kidridicarus.guide.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import kidridicarus.tools.EncapTexAtlas;

public class HudCoin extends Image {
	private CoinHudDrawable drawable;

	public HudCoin(EncapTexAtlas encapTexAtlas) {
		super();
		setDrawable(drawable = new CoinHudDrawable(encapTexAtlas));
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		drawable.act(delta);
	}
}