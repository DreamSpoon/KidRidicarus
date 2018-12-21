package kidridicarus.agent.sprites.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.agent.SMB.enemy.Turtle.TurtleState;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class TurtleSprite extends Sprite {
	private static final float ANIM_SPEED = 0.25f;

	private Animation<TextureRegion> walkAnimation;
	private TextureRegion insideShell;
	private Animation<TextureRegion> wakeUpAnimation;
	private float stateTimer;

	public TurtleSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_TURTLE, 0, 0, 16, 24));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_TURTLE, 16, 0, 16, 24));
		walkAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		frames.clear();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_TURTLE, 5 * 16, 0, 16, 24));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_TURTLE, 4 * 16, 0, 16, 24));
		wakeUpAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);

		insideShell = encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_TURTLE, 4 * 16, 0, 16, 24);

		stateTimer = 0;

		setRegion(walkAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(16), UInfo.P2M(24));
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
