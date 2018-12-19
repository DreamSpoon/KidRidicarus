package kidridicarus.agent.sprites.SMB.item;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.info.UInfo;

public class MushroomSprite extends Sprite {
	public MushroomSprite(TextureRegion textureRegion, Vector2 position) {
		super(textureRegion);
		setBounds(getX(), getY(), UInfo.P2M(UInfo.TILEPIX_X), UInfo.P2M(UInfo.TILEPIX_Y));
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position) {
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
