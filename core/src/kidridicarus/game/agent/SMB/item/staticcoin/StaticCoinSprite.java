package kidridicarus.game.agent.SMB.item.staticcoin;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMB_Gfx;

public class StaticCoinSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.133f;

	private Animation<TextureRegion> coinAnim;

	public StaticCoinSprite(TextureAtlas atlas, Vector2 position) {
		coinAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMB_Gfx.Item.COIN_STATIC), PlayMode.LOOP);

		setRegion(coinAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float globalTimer) {
		setRegion(coinAnim.getKeyFrame(globalTimer));
	}
}

