package kidridicarus.sprites.SMB;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class Mush1UPSprite extends Sprite {
	public Mush1UPSprite(TextureAtlas atlas, Vector2 position) {
		super(atlas.findRegion(GameInfo.TEXATLAS_MUSH1UP));
		setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
