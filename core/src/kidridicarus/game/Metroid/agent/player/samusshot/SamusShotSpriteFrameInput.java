package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShotBrain.MoveState;

public class SamusShotSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public SamusShotSpriteFrameInput(Vector2 position, float timeDelta, MoveState moveState) {
		super(false, timeDelta, false, false, 0f, position);
		this.moveState = moveState;
	}
}
