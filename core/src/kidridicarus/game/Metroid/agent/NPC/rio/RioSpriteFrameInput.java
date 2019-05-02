package kidridicarus.game.Metroid.agent.NPC.rio;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.Metroid.agent.NPC.rio.RioBrain.MoveState;

public class RioSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public RioSpriteFrameInput() {
		super();
		moveState = null;
	}

	public RioSpriteFrameInput(Vector2 position, float timeDelta, MoveState moveState) {
		super(false, timeDelta, false, false, 0f, position);
		this.moveState = moveState;
	}
}
