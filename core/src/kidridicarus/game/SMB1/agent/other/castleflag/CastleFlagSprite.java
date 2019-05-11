package kidridicarus.game.SMB1.agent.other.castleflag;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.SMB1_Gfx;

class CastleFlagSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	CastleFlagSprite(TextureAtlas atlas, Vector2 position) {
		setRegion(atlas.findRegion(SMB1_Gfx.General.CASTLEFLAG));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}
}
