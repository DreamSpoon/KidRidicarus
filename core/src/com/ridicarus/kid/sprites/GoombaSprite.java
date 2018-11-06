package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.robot.Goomba.GoombaState;

public class GoombaSprite extends Sprite {
	private float stateTime;
	private Animation<TextureRegion> walkAnimation;
	private TextureRegion squish;

	public GoombaSprite(TextureAtlas atlas) {
		super(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA));

		Array<TextureRegion> frames = new Array<TextureRegion>();
		for(int i = 0; i < 2; i++)
			frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA), i * 16, 0, 16, 16));
		walkAnimation = new Animation<TextureRegion>(0.4f, frames);

		squish = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA), 2 * 16, 0, 16, 16);

		stateTime = 0;
	}

	public void update(float delta, GoombaState curState) {
		switch(curState) {
		case WALK:
		case FALL:
			setRegion(walkAnimation.getKeyFrame(stateTime, true));
			break;
		case DEAD:
			setRegion(squish);
			break;
		}

		stateTime += delta;
	}
}
