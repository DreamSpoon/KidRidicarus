package kidridicarus.agent.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.SMBAnim;
import kidridicarus.info.UInfo;

public class BrickPieceSprite extends Sprite {
	private static final int SPRITE_WIDTH = 8;
	private static final int SPRITE_HEIGHT = 8;
	private static final float ANIM_SPEED = 0.2f;
	private Animation<TextureRegion> spinAnimation;
	private float stateTimer;

	public BrickPieceSprite(TextureAtlas atlas, Vector2 position, int startFrame) {
		spinAnimation = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.General.BRICKPIECE), PlayMode.LOOP);

		stateTimer = (float) startFrame * ANIM_SPEED;

		setRegion(spinAnimation.getKeyFrame(0f));
		setBounds(getX(), getY(), UInfo.P2M(SPRITE_WIDTH), UInfo.P2M(SPRITE_HEIGHT));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position, float delta) {
		setRegion(spinAnimation.getKeyFrame(stateTimer, true));
		stateTimer += delta;

		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
