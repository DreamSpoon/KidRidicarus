package kidridicarus.game.Metroid.agent.NPC.zoomer;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.NPC.zoomer.ZoomerBrain.MoveState;

class ZoomerSpriteFrameInput extends SpriteFrameInput {
	MoveState moveState;

	ZoomerSpriteFrameInput(Vector2 position, FrameTime frameTime, MoveState moveState, Direction4 upDir) {
		super(frameTime, false, false, getRotationForUpDir(upDir), position);
		this.moveState = moveState;
	}

	private static float getRotationForUpDir(Direction4 upDir) {
		switch(upDir) {
			case RIGHT:
				return 270f;
			case UP:
			default:
				return 0f;
			case LEFT:
				return 90f;
			case DOWN:
				return 180f;
		}
	}
}
