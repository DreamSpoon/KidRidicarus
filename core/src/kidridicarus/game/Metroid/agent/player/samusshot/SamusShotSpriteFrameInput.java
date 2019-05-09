package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShotBrain.MoveState;

public class SamusShotSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public SamusShotSpriteFrameInput(Vector2 position, FrameTime frameTime, MoveState moveState) {
		super(frameTime, false, false, 0f, position);
		this.moveState = moveState;
	}
}
