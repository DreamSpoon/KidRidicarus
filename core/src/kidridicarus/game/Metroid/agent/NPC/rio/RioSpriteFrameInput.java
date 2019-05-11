package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.game.Metroid.agent.NPC.rio.RioBrain.MoveState;

class RioSpriteFrameInput extends SpriteFrameInput {
	MoveState moveState;

	RioSpriteFrameInput() {
		super();
		moveState = null;
	}

	RioSpriteFrameInput(Vector2 position, FrameTime frameTime, MoveState moveState) {
		super(frameTime, false, false, 0f, position);
		this.moveState = moveState;
	}
}
