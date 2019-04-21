package kidridicarus.game.KidIcarus.agent.NPC.shemum;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class ShemumSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2/15f;

	private Animation<TextureRegion> walkAnim;
	private float stateTimer;

	public ShemumSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(KidIcarusGfx.NPC.SHEMUM), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, boolean isFacingRight) {
		setRegion(walkAnim.getKeyFrame(stateTimer));
		if((isFacingRight && isFlipX()) || (!isFacingRight && !isFlipX()))
			flip(true,  false);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		stateTimer += delta;
	}
}
