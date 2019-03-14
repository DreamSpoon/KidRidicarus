package kidridicarus.game.agent.SMB.item.powerstar;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMBAnim;

public class PowerStarSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.075f;

	private Animation<TextureRegion> starAnim;
	private float stateTimer;

	public PowerStarSprite(TextureAtlas atlas, Vector2 position) {
		starAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.Item.POWERSTAR), PlayMode.LOOP);

		stateTimer = 0f;

		setRegion(starAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setRegion(starAnim.getKeyFrame(stateTimer));
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
		stateTimer += delta;
	}
}
