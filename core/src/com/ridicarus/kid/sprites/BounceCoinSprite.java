package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;

public class BounceCoinSprite extends Sprite {
	private static final float ANIM_SPEED = 1f / 30f;
	private float stateTimer;
	private Animation<TextureRegion> spinAnimation;

	public BounceCoinSprite(TextureAtlas atlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		// add the frames in the correct order
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 1 * 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 0 * 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 3 * 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_COIN_SPIN), 2 * 16, 0, 16, 16));
		spinAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = 0;
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		setBounds(getX(), getY(), GameInfo.P2M(16), GameInfo.P2M(16));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		stateTimer += delta;
	}
}
