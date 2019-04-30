package kidridicarus.agency.agentsprite;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class AgentSprite extends Sprite {
	private boolean isVisible = false;

	public void processFrame(SpriteFrameInput frameInput) {
		if(preFrameInput(frameInput))
			postFrameInput(frameInput);
	}

	/*
	 * Set visibility flag, and return flag for convenience.
	 * Returns frame visibility flag if available, otherwise returns false.
	 * This function is for convenience and is optional.
	 */
	protected boolean preFrameInput(SpriteFrameInput frameInput) {
		this.isVisible = frameInput != null;
		return this.isVisible;
	}

	/*
	 * Apply frame input, including set visibility flag.
	 * This function is "more or less" required.
	 */
	protected void postFrameInput(SpriteFrameInput frameInput) {
		isVisible = frameInput != null;
		if(!isVisible)
			return;
		flip(frameInput.flipX ^ isFlipX(), frameInput.flipY ^ isFlipY());
		setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
		// rotate about the center of the sprite
		setOrigin(getWidth()/2f, getHeight()/2f);
		setRotation(frameInput.rotation);
	}

	@Override
	public void draw(Batch batch) {
		if(isVisible)
			super.draw(batch);
	}
}
