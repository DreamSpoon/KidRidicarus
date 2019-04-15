package kidridicarus.game.agent.KidIcarus.NPC.specknose;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.basic.AnimSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class SpecknoseSprite extends AnimSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 1f;

	public SpecknoseSprite(TextureAtlas atlas, Vector2 position) {
		super(new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.NPC.SPECKNOSE),
				PlayMode.LOOP), SPRITE_WIDTH, SPRITE_HEIGHT, position);
	}
}
