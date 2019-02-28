package kidridicarus.game.agentsprite.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.info.SMBAnim;

public class BounceCoinSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 1f / 30f;

	private Animation<TextureRegion> spinAnim;
	private float stateTimer;

	public BounceCoinSprite(TextureAtlas atlas, Vector2 position) {
		spinAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.General.COIN_SPIN), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(spinAnim.getKeyFrame(stateTimer, true));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setRegion(spinAnim.getKeyFrame(stateTimer, true));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		stateTimer += delta;
	}
}
