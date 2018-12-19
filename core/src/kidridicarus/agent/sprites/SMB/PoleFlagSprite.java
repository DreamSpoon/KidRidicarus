package kidridicarus.agent.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.tools.EncapTexAtlas;

public class PoleFlagSprite extends Sprite {
	public PoleFlagSprite(EncapTexAtlas encapTexAtlas, Vector2 position) {
		super(encapTexAtlas.findSubRegion(GameInfo.TEXATLAS_FLAG, 0, 0, 16, 16));
		setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position) {
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
