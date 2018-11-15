/*
 * By: David Loucks
 * Approx. Date: 2018.11.08
*/

package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;

public class CoinSpinSprite extends Sprite {
	private float stateTimer;
	private Animation<TextureRegion> spinAnimation;

	public CoinSpinSprite(TextureAtlas atlas, float animSpeed) {
		super(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN));

		Array<TextureRegion> frames = new Array<TextureRegion>();
		// add the frames in the correct order
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 1 * 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 0 * 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 3 * 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 2 * 16, 0, 16, 16));
		spinAnimation = new Animation<TextureRegion>(animSpeed, frames);

		stateTimer = 0;
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
	}

	public void update(float delta) {
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		stateTimer += delta;
	}
}
