package com.ridicarus.kid.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.robot.Turtle.TurtleState;

public class TurtleSprite extends Sprite {
	private static final float WALK_ANIM_SPEED = 0.4f;

	private float stateTime;
	private Animation<TextureRegion> walkAnimation;
	private TextureRegion insideShell;

	public TurtleSprite(TextureAtlas atlas, float x, float y) {
		super(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 0, 0, 16, 24));
		setPosition(x, y);
		setBounds(getX(), getY(), GameInfo.P2M(16), GameInfo.P2M(24));

		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 0, 0, 16, 24));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 16, 0, 16, 24));
		walkAnimation = new Animation<TextureRegion>(WALK_ANIM_SPEED, frames);

		insideShell = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 4 * 16, 0, 16, 24);

		stateTime = 0;
	}

	public void update(float delta, Vector2 position, TurtleState curState, boolean facingRight) {
		switch(curState) {
			case WALK:
				setRegion(walkAnimation.getKeyFrame(stateTime, true));
				break;
			case HIDE:
			case SLIDE:
				setRegion(insideShell);
				break;
			case DEAD:
				setRegion(insideShell);
				// upside down when dead
				if(!isFlipY())
					flip(false,  true);
				break;
		}

		// Ensure the sprite is facing the correct direction
		// (the turtle sprite faces left in the spritesheet, so left/right is reversed)
		if(facingRight && !isFlipX())
			flip(true, false);
		else if(!facingRight && isFlipX())
			flip(true, false);

		setPosition(position.x - getWidth() / 2f, position.y - getHeight() * 0.375f);

		stateTime += delta;
	}
}
