package kidridicarus.game.agent.SMB.other.castleflag;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.SMBAnim;

public class CastleFlagSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public CastleFlagSprite(TextureAtlas atlas, Vector2 position) {
		super(atlas.findRegion(SMBAnim.General.CASTLEFLAG));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position) {
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
