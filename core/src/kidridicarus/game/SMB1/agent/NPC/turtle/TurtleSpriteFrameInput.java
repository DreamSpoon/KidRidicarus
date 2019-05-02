package kidridicarus.game.SMB1.agent.NPC.turtle;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.SMB1.agent.NPC.turtle.TurtleBrain.MoveState;

public class TurtleSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public TurtleSpriteFrameInput(Vector2 position, boolean isFacingRight, float timeDelta, MoveState moveState) {
		super(false, timeDelta, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
