/*
 * By: David Loucks
 * Approx. Date: 2018.11.08
*/

package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;

public class BrickPieceSprite extends Sprite {
	private static final float ANIM_SPEED = 0.2f;
	private Animation<TextureRegion> spinAnimation;
	private float stateTimer;

	public BrickPieceSprite(TextureAtlas atlas, Vector2 position, float size, int startFrame) {
		super(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BRICKPIECES), 0, 0, 8, 8));
		Array<TextureRegion> frames;

		// add the frames in the correct order
		frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BRICKPIECES), 0, 0, 8, 8));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BRICKPIECES), 8, 8, 8, 8));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BRICKPIECES), 8, 0, 8, 8));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_BRICKPIECES), 0, 8, 8, 8));
		spinAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		stateTimer = (float) startFrame * ANIM_SPEED;

		setPosition(position.x - size/2f, position.y - size/2f);
		setBounds(getX(), getY(), size, size);
	}

	public void update(Vector2 position, float delta) {
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		stateTimer += delta;

		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
