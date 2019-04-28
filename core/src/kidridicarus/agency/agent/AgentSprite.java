package kidridicarus.agency.agent;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import kidridicarus.common.agentsprite.SpriteFrameInput;

public abstract class AgentSprite extends Sprite {
	public abstract void processFrame(SpriteFrameInput frameInput);

	private boolean isVisible;

	// TODO Remove this contstructor, set isVisible=false by default, and sprite will call method
	// applyFrameInput in their constructor to set the visible flag.
	public AgentSprite(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override
	public void draw(Batch batch) {
		if(isVisible)
			super.draw(batch);
	}

	// apply the inputs in frameInput to this Sprite
	protected void applyFrameInput(SpriteFrameInput frameInput) {
		isVisible = frameInput.visible;
		if((frameInput.flipX && !isFlipX()) || (!frameInput.flipX && isFlipX()))
			flip(true,  false);
		setPosition(frameInput.position.x - getWidth()/2f, frameInput.position.y - getHeight()/2f);
	}
}
