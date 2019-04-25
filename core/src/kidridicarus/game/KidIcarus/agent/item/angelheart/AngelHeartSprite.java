package kidridicarus.game.KidIcarus.agent.item.angelheart;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeartBrain.AngelHeartSize;
import kidridicarus.game.info.KidIcarusGfx;

public class AngelHeartSprite extends AgentSprite {
	private static final float SML_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SML_SPRITE_HEIGHT = UInfo.P2M(8);
	private static final float MED_SPRITE_WIDTH = UInfo.P2M(8);
	private static final float MED_SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float BIG_SPRITE_WIDTH = UInfo.P2M(16);
	private static final float BIG_SPRITE_HEIGHT = UInfo.P2M(16);

	public AngelHeartSprite(TextureAtlas atlas, Vector2 position, AngelHeartSize heartSize) {
		super(true);
		switch(heartSize) {
			case FULL:
				setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART10));
				setBounds(getX(), getY(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
				break;
			case HALF:
				setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART5));
				setBounds(getX(), getY(), MED_SPRITE_WIDTH, MED_SPRITE_HEIGHT);
				break;
			case SMALL:
			default:
				setRegion(atlas.findRegion(KidIcarusGfx.Item.HEART1));
				setBounds(getX(), getY(), SML_SPRITE_WIDTH, SML_SPRITE_HEIGHT);
				break;
		}
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		isVisible = frameInput.visible;
		if((frameInput.flipX && !isFlipX()) || (!frameInput.flipX && isFlipX()))
			flip(true,  false);
		setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
	}
}