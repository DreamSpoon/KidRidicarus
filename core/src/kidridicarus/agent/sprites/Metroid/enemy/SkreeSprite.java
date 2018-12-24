package kidridicarus.agent.sprites.Metroid.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import kidridicarus.agent.Metroid.enemy.Skree.SkreeState;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class SkreeSprite extends Sprite {
	private static final float ANIM_SPEED_REG = 0.05f;
	private static final float ANIM_SPEED_FAST = ANIM_SPEED_REG / 2f;

	private static final Vector2 SPECIAL_OFFSET = UInfo.P2MVector(0f, 2f);

	// regular and fast flap wings animations
	private Animation<TextureRegion> flapRegAnimation;
	private Animation<TextureRegion> flapFastAnimation;

	private float stateTimer;

	public SkreeSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_M_SKREE, 0, 0, 16, 24));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_M_SKREE, 16, 0, 16, 24));
		frames.add(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_M_SKREE, 32, 0, 16, 24));
		flapRegAnimation = new Animation<TextureRegion>(ANIM_SPEED_REG, frames);
		flapFastAnimation = new Animation<TextureRegion>(ANIM_SPEED_FAST, frames);

		stateTimer = 0;

		setRegion(flapRegAnimation.getKeyFrame(0f));
		setBounds(getX(), getY()+16, UInfo.P2M(16), UInfo.P2M(24));
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}

	public void update(float delta, Vector2 position, SkreeState curState) {
		switch(curState) {
			case SLEEP:
				setRegion(flapRegAnimation.getKeyFrame(stateTimer, true));
				break;
			case FALL:
				setRegion(flapFastAnimation.getKeyFrame(stateTimer, true));
				break;
			case DEAD:
				break;
		}

		stateTimer += delta;
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}
}
