package kidridicarus.agent.sprites.Metroid.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Metroid.enemy.Skree.SkreeState;
import kidridicarus.info.MetroidAnim;
import kidridicarus.info.UInfo;

public class SkreeSprite extends Sprite {
	private static final int SPRITE_WIDTH = 16;
	private static final int SPRITE_HEIGHT = 24;
	private static final float ANIM_SPEED_REG = 0.05f;
	private static final float ANIM_SPEED_FAST = ANIM_SPEED_REG / 2f;

	private static final Vector2 SPECIAL_OFFSET = UInfo.P2MVector(0f, 2f);

	// regular and fast flap wings animations
	private Animation<TextureRegion> flapRegAnim;
	private Animation<TextureRegion> flapFastAnim;

	private float stateTimer;

	public SkreeSprite(TextureAtlas atlas, Vector2 position) {
		flapRegAnim = new Animation<TextureRegion>(ANIM_SPEED_REG,
				atlas.findRegions(MetroidAnim.Enemy.SKREE), PlayMode.LOOP);
		flapRegAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidAnim.Enemy.SKREE), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(flapRegAnim.getKeyFrame(0f));
		setBounds(getX(), getY()+16, UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}

	public void update(float delta, Vector2 position, SkreeState curState) {
		switch(curState) {
			case SLEEP:
				setRegion(flapRegAnim.getKeyFrame(stateTimer, true));
				break;
			case FALL:
				setRegion(flapFastAnim.getKeyFrame(stateTimer, true));
				break;
			case DEAD:
				break;
		}

		stateTimer += delta;
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}
}
