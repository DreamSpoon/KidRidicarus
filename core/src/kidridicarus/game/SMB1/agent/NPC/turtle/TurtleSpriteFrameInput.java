package kidridicarus.game.SMB1.agent.NPC.turtle;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.game.SMB1.agent.NPC.turtle.TurtleBrain.MoveState;

public class TurtleSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public TurtleSpriteFrameInput(Vector2 position, boolean isFacingRight, FrameTime frameTime, MoveState moveState) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
