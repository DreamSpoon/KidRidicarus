package kidridicarus.game.KidIcarus.agent.item.chalicehealth;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class ChaliceHealthSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	public ChaliceHealthSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		setRegion(atlas.findRegion(KidIcarusGfx.Item.CHALICE));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		applyFrameInput(frameInput);
	}
}
