package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoorBrain.MoveState;

public class MetroidDoorSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public MetroidDoorSpriteFrameInput(Vector2 position, boolean isFacingRight, FrameTime frameTime,
			MoveState moveState) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
