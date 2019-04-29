package kidridicarus.game.SMB1.agent.NPC.turtle;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.SMB1.agent.NPC.turtle.TurtleBrain.MoveState;

public class TurtleSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;

	public TurtleSpriteFrameInput(boolean visible, Vector2 position, boolean isFacingRight, float timeDelta,
			MoveState moveState) {
		super(visible, position, isFacingRight, timeDelta);
		this.moveState = moveState;
	}
}
