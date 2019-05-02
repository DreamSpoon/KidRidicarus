package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoorBrain.MoveState;

public class MetroidDoorSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public MetroidDoorSpriteFrameInput(Vector2 position, boolean isFacingRight, float timeDelta, MoveState moveState) {
		super(false, timeDelta, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
