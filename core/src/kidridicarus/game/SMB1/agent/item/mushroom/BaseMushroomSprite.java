package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB1.agent.item.fireflower.SproutSpriteFrameInput;

public class BaseMushroomSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);

	private BaseMushroom parent;
	private AgentDrawListener myDrawListener;

	public BaseMushroomSprite(BaseMushroom parent, TextureRegion texRegion, Vector2 position) {
		this.parent = parent;
		setRegion(texRegion);
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		applyFrameInput(new SpriteFrameInput(position));

		final BaseMushroomSprite self = this;
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(self); }
			};
		parent.getAgency().addAgentDrawListener(parent, CommonInfo.DrawOrder.SPRITE_BOTTOM, myDrawListener);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(frameInput == null)
			return;
		if(((SproutSpriteFrameInput) frameInput).finishSprout)
			finishSprout();
		applyFrameInput(frameInput);
	}

	private void finishSprout() {
		final BaseMushroomSprite self = this;
		// change from bottom to middle sprite draw order
		parent.getAgency().removeAgentDrawListener(parent, myDrawListener);
		myDrawListener = new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(self); }
			};
		parent.getAgency().addAgentDrawListener(parent, CommonInfo.DrawOrder.SPRITE_MIDDLE, myDrawListener);
	}
}
