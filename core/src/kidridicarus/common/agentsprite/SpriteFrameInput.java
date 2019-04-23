package kidridicarus.common.agentsprite;

import com.badlogic.gdx.math.Vector2;

public class SpriteFrameInput {
	public boolean visible;
	public Vector2 position;
	public boolean flipX;	// flipX = !isFacingRight;

	public SpriteFrameInput(boolean visible, Vector2 position, boolean flipX) {
		this.visible = visible;
		this.position = position;
		this.flipX = flipX;
	}
}
