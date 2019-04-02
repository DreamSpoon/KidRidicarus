package kidridicarus.game.agent.SMB1.other.brickpiece;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMB1_Gfx;

public class BrickPieceSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float ANIM_SPEED = 0.2f;
	private Animation<TextureRegion> spinAnim;
	private float stateTimer;

	public BrickPieceSprite(TextureAtlas atlas, Vector2 position, int startFrame) {
		spinAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.General.BRICKPIECE), PlayMode.LOOP);

		stateTimer = (float) startFrame * ANIM_SPEED;

		setRegion(spinAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position, float delta) {
		setRegion(spinAnim.getKeyFrame(stateTimer));
		stateTimer += delta;

		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
