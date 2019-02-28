package kidridicarus.game.agentsprite.SMB.item;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;

public class MushroomSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public MushroomSprite(TextureRegion textureRegion, Vector2 position) {
		super(textureRegion);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position) {
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
