package kidridicarus.game.KidIcarus.agent.NPC.monoeye;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.KidIcarusGfx;

public class MonoeyeSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public MonoeyeSprite(TextureAtlas atlas, Vector2 position) {
		setRegion(atlas.findRegion(KidIcarusGfx.NPC.MONOEYE));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}
}
