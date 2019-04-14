package kidridicarus.game.agent.SMB1.item.mushroom;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.basic.AnimSprite;
import kidridicarus.common.info.UInfo;

public class BaseMushroomSprite extends AnimSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public BaseMushroomSprite(Animation<TextureRegion> mushroomAnim, Vector2 position) {
		super(mushroomAnim, SPRITE_WIDTH, SPRITE_HEIGHT, position);
	}
}
