package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class KidIcarusDoorSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(32);

	private TextureRegion closedTexRegion;
	private TextureRegion openedTexRegion;

	public KidIcarusDoorSprite(TextureAtlas atlas, Vector2 position, boolean isOpened) {
		super(true);
		closedTexRegion = atlas.findRegion(KidIcarusGfx.General.DOOR_BROWN_CLOSED);
		openedTexRegion = atlas.findRegion(KidIcarusGfx.General.DOOR_BROWN_OPENED);
		setRegion(isOpened ? openedTexRegion : closedTexRegion);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		setRegion(((KidIcarusDoorSpriteFrameInput) frameInput).isOpened ? openedTexRegion : closedTexRegion);
		applyFrameInput(frameInput);
	}
}
