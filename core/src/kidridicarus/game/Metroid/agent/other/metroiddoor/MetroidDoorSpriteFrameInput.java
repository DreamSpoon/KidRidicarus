package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.game.Metroid.agent.other.metroiddoor.MetroidDoorBrain.MoveState;

class MetroidDoorSpriteFrameInput extends SpriteFrameInput {
	MoveState moveState;

	MetroidDoorSpriteFrameInput(Vector2 position, boolean isFacingRight, FrameTime frameTime,
			MoveState moveState) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
