package kidridicarus.game.SMB1.agent.NPC.goomba;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.SMB1.agent.NPC.goomba.GoombaBrain.MoveState;

public class GoombaSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;

	public GoombaSpriteFrameInput(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
	}
}
