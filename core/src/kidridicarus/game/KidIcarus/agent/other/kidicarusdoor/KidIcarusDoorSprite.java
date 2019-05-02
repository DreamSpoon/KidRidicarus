package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.SprFrameTool;
import kidridicarus.game.info.KidIcarusGfx;

public class KidIcarusDoorSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(32);

	private TextureRegion closedTex;
	private TextureRegion openedTex;

	public KidIcarusDoorSprite(TextureAtlas atlas, Vector2 position, boolean isOpened) {
		closedTex = atlas.findRegion(KidIcarusGfx.General.DOOR_BROWN_CLOSED);
		openedTex = atlas.findRegion(KidIcarusGfx.General.DOOR_BROWN_OPENED);
		setRegion(isOpened ? openedTex : closedTex);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(SprFrameTool.place(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		setRegion(((KidIcarusDoorSpriteFrameInput) frameInput).isOpened ? openedTex : closedTex);
		postFrameInput(frameInput);
	}
}
