package com.ridicarus.kid.hud;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class HudCoin extends Image {
	private CoinHudDrawable drawable;

	public HudCoin(TextureAtlas atlas) {
		super();
		setDrawable(drawable = new CoinHudDrawable(atlas));
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		drawable.act(delta);
	}
}