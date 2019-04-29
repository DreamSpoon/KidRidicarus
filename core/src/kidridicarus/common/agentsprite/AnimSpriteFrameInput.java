package kidridicarus.common.agentsprite;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;

public class AnimSpriteFrameInput extends SpriteFrameInput {
	public float timeDelta;

	public AnimSpriteFrameInput(boolean visible, Vector2 position, boolean isFacingRight, float timeDelta) {
		super(visible, position, false, !isFacingRight, false);
		this.timeDelta = timeDelta;
	}

	public AnimSpriteFrameInput(Vector2 position, boolean isFacingRight, float timeDelta) {
		super(position, isFacingRight);
		this.timeDelta = timeDelta;
	}
}
