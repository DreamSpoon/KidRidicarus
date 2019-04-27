package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.Metroid.agent.player.samusshot.SamusShotBrain.MoveState;

public class SamusShotSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;

	public SamusShotSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
	}
}
