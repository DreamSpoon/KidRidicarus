package kidridicarus.game.Metroid.agent.NPC.zoomer;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.NPC.zoomer.ZoomerBrain.MoveState;

public class ZoomerSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;
	public Direction4 upDir;

	public ZoomerSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState, Direction4 upDir) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
		this.upDir = upDir;
	}
}
