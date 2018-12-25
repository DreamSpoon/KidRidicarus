package kidridicarus.agent.sprites.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.SMB.enemy.Turtle.TurtleState;
import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class TurtleSprite extends Sprite {
	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 24;
	private static final float ANIM_SPEED = 0.25f;

	private Animation<TextureRegion> walkAnimation;
	private TextureRegion insideShell;
	private Animation<TextureRegion> wakeUpAnimation;
	private float stateTimer;

	public TurtleSprite(TextureAtlas atlas, Vector2 position) {
		walkAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.Enemy.TURTLE_WALK), PlayMode.LOOP);
		wakeUpAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.Enemy.TURTLE_WAKEUP), PlayMode.LOOP);
		insideShell = atlas.findRegion(SMBAnim.Enemy.TURTLE_HIDE);

		stateTimer = 0;

		setRegion(walkAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, TurtleState curState, boolean facingRight) {
		switch(curState) {
			case WALK:
				setRegion(walkAnimation.getKeyFrame(stateTimer, true));
				break;
			case HIDE:
			case SLIDE:
				setRegion(insideShell);
				break;
			case WAKE_UP:
				setRegion(wakeUpAnimation.getKeyFrame(stateTimer, true));
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

		stateTimer += delta;
	}
}
