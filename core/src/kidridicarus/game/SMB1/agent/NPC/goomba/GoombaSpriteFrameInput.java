package kidridicarus.game.SMB1.agent.NPC.goomba;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.SMB1.agent.NPC.goomba.GoombaBrain.MoveState;

public class GoombaSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;

	public GoombaSpriteFrameInput(Vector2 position, boolean isFacingRight, float timeDelta, MoveState moveState) {
		super(false, timeDelta, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
