package kidridicarus.agent.sprites.SMB.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.agent.SMB.enemy.Goomba.GoombaState;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class GoombaSprite extends Sprite {
	private static final float ANIM_SPEED = 0.4f;

	private Animation<TextureRegion> walkAnimation;
	private TextureRegion squish;
	private float stateTimer;

	public GoombaSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_GOOMBA, 0, 0, 16, 16));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_GOOMBA, 16, 0, 16, 16));
		walkAnimation = new Animation<TextureRegion>(ANIM_SPEED, frames);
		squish = encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_GOOMBA, 2 * 16, 0, 16, 16);

		stateTimer = 0;

		setRegion(walkAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(16), UInfo.P2M(16));
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
