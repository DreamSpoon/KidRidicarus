package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;

public class PowerStarSprite extends Sprite {
	private static final float ANIM_SPEED = 0.075f;

	private Animation<TextureRegion> starAnimation;
	private float stateTimer;

	public PowerStarSprite(TextureAtlas atlas, Vector2 position) {
		super(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 0, 0, 16, 16));
		setBounds(getX(), getY(), GameInfo.P2M(GameInfo.TILEPIX_X), GameInfo.P2M(GameInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 0, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 16, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 32, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_POWERSTAR), 48, 0, 16, 16));
		starAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = 0f;
	}

	public void update(float delta, Vector2 position) {
		setRegion(starAnimation.getKeyFrame(stateTimer, true));

		// update sprite position
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);

		stateTimer += delta;
	}
}
