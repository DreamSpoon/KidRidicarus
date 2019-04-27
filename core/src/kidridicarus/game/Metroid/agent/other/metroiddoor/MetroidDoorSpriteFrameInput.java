package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoorBrain.MoveState;

public class MetroidDoorSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;

	public MetroidDoorSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
	}
}
