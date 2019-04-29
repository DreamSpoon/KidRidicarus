package kidridicarus.agency.agentsprite;

import com.badlogic.gdx.math.Vector2;

public class SpriteFrameInput {
	public boolean visible;
	public Vector2 position;
	public boolean absPosition;
	public boolean flipX;	// flipX = !isFacingRight;
	public boolean flipY;	// flipY = !isFacingUp;

	public SpriteFrameInput(boolean visible, Vector2 position, boolean absPosition, boolean flipX, boolean flipY) {
		this.visible = visible;
		this.position = position;
		this.absPosition = absPosition;
		this.flipX = flipX;
		this.flipY = flipY;
	}

	public SpriteFrameInput(Vector2 position, boolean isFacingRight) {
		this(true, position, false, !isFacingRight, false);
	}

	public SpriteFrameInput(Vector2 position) {
		this(true, position, false, false, false);
	}

	public SpriteFrameInput(SpriteFrameInput frameInput) {
		this.visible = frameInput.visible;
		this.position = frameInput.position.cpy();
		this.absPosition = frameInput.absPosition;
		this.flipX = frameInput.flipX;
		this.flipY = frameInput.flipY;
	}
}
