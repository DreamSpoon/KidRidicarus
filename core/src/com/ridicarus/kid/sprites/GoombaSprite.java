/*
 * Originally created by brentaureli on 9/14/15.
 * GitHub:
 *     https://github.com/BrentAureli/SuperMario
 * 
 * Retrieved from GitHub on Oct 17, 2018
 * Modified afterward by David Loucks.
*/

package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.robot.Goomba.GoombaState;

public class GoombaSprite extends Sprite {
	private float stateTimer;
	private Animation<TextureRegion> walkAnimation;
	private TextureRegion squish;

	public GoombaSprite(TextureAtlas atlas, float x, float y) {
		super(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA), 0, 0, 16, 16));
		setPosition(x, y);
		setBounds(getX(), getY(), GameInfo.P2M(16), GameInfo.P2M(16));

		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA), 0, 0, 16, 16));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA), 16, 0, 16, 16));
		walkAnimation = new Animation<TextureRegion>(0.4f, frames);

		squish = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_GOOMBA), 2 * 16, 0, 16, 16);

		stateTimer = 0;
	}

	public void update(float delta, Vector2 position, GoombaState curState) {
		switch(curState) {
			case WALK:
			case FALL:
				setRegion(walkAnimation.getKeyFrame(stateTimer, true));
				break;
			case DEAD_SQUISH:
				setRegion(squish);
				break;
			case DEAD_BUMPED:
				// no walking after bopping
				setRegion(walkAnimation.getKeyFrame(0, true));
				// upside down when bopped
				if(!isFlipY())
					flip(false,  true);
				break;
		}

		stateTimer += delta;

		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
