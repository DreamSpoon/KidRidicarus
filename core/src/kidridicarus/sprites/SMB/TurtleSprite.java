package kidridicarus.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.GameInfo;
import kidridicarus.roles.robot.SMB.enemy.TurtleRole.TurtleState;

public class TurtleSprite extends Sprite {
	private static final float WALK_ANIM_SPEED = 0.25f;

	private float stateTimer;
	private Animation<TextureRegion> walkAnimation;
	private TextureRegion insideShell;
	private Animation<TextureRegion> wakeUpAnimation;

	public TurtleSprite(TextureAtlas atlas, Vector2 position) {
		super(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 0, 0, 16, 24));
		setBounds(getX(), getY(), GameInfo.P2M(16), GameInfo.P2M(24));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);

		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 0, 0, 16, 24));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 16, 0, 16, 24));
		walkAnimation = new Animation<TextureRegion>(WALK_ANIM_SPEED, frames);

		frames.clear();
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 5 * 16, 0, 16, 24));
		frames.add(new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 4 * 16, 0, 16, 24));
		wakeUpAnimation = new Animation<TextureRegion>(WALK_ANIM_SPEED, frames);

		insideShell = new TextureRegion(atlas.findRegion(GameInfo.TEXATLAS_TURTLE), 4 * 16, 0, 16, 24);

		stateTimer = 0;
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
