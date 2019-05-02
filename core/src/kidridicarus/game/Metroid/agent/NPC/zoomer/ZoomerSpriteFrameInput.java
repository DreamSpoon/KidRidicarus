package kidridicarus.game.Metroid.agent.NPC.zoomer;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.NPC.zoomer.ZoomerBrain.MoveState;

public class ZoomerSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public ZoomerSpriteFrameInput(Vector2 position, float timeDelta, MoveState moveState, Direction4 upDir) {
		super(false, timeDelta, false, false, getRotationForUpDir(upDir), position);
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
