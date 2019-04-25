package kidridicarus.agency.agent;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import kidridicarus.common.agentsprite.SpriteFrameInput;

public abstract class AgentSprite extends Sprite {
	protected boolean isVisible;

	public AgentSprite(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public abstract void processFrame(SpriteFrameInput frameInput);

	@Override
	public void draw(Batch batch) {
		if(isVisible)
			super.draw(batch);
	}
}
