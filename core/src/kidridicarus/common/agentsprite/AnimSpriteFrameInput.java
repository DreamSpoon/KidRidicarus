package kidridicarus.common.agentsprite;

import com.badlogic.gdx.math.Vector2;

public class AnimSpriteFrameInput extends SpriteFrameInput {
	public float timeDelta;

	public AnimSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta) {
		super(visible, position, flipX);
		this.timeDelta = timeDelta;
	}
}
