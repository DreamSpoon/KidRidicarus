package kidridicarus.game.agentsprite.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.info.MetroidAnim;

public class SkreeExpSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(8);

	public SkreeExpSprite(TextureAtlas atlas, Vector2 position) {
		setRegion(atlas.findRegion(MetroidAnim.NPC.SKREE_EXP));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(Vector2 position) {
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
