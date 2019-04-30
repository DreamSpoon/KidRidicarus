package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.Metroid.agent.NPC.rio.RioBrain.MoveState;

public class RioSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;

	public RioSpriteFrameInput() {
		super();
		moveState = null;
	}

	public RioSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
	}
}
