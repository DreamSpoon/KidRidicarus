package kidridicarus.agency.guide.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import kidridicarus.agency.Agency;

public class HudCoin extends Image {
	private Agency agency;
	private CoinHudDrawable drawable;

	public HudCoin(Agency agency) {
		super();
		this.agency = agency;
		drawable = new CoinHudDrawable(agency.getAtlas());
		setDrawable(drawable);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		drawable.act(agency.getGlobalTimer());
	}
}