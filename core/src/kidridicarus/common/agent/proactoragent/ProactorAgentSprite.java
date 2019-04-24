package kidridicarus.common.agent.proactoragent;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import kidridicarus.common.agentsprite.SpriteFrameInput;

public abstract class ProactorAgentSprite extends Sprite {
	protected boolean isVisible = true;

	public abstract void processFrame(SpriteFrameInput frameInput);

	@Override
	public void draw(Batch batch) {
		if(isVisible)
			super.draw(batch);
	}
}
