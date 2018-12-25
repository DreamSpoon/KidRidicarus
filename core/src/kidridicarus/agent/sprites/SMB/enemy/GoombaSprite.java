package kidridicarus.agent.sprites.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.SMB.enemy.Goomba.GoombaState;
import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class GoombaSprite extends Sprite {
	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 16;
	private static final float ANIM_SPEED = 0.4f;

	private Animation<TextureRegion> walkAnimation;
	private TextureRegion squish;
	private float stateTimer;

	public GoombaSprite(TextureAtlas atlas, Vector2 position) {
		walkAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.Enemy.GOOMBA_WALK), PlayMode.LOOP);
		squish = atlas.findRegion(SMBAnim.Enemy.GOOMBA_SQUISH);

		stateTimer = 0;

		setRegion(walkAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
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
