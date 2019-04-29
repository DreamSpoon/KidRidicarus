package kidridicarus.agency.agentsprite;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class AgentSprite extends Sprite {
	private boolean isVisible = true;

	/*
	 * Default method, override as needed. Subclasses do not need to call this method, but should call
	 * applyFrameInput or implement functionality otherwise.
	 */
	public void processFrame(SpriteFrameInput frameInput) {
		applyFrameInput(frameInput);
	}

	protected void applyFrameInput(SpriteFrameInput frameInput) {
		isVisible = frameInput.visible;
		flip(frameInput.flipX ^ isFlipX(), frameInput.flipY ^ isFlipY());
		if(frameInput.absPosition)
			setPosition(frameInput.position.x, frameInput.position.y);
		else
			setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
	}

	@Override
	public void draw(Batch batch) {
		if(isVisible)
			super.draw(batch);
	}
}
