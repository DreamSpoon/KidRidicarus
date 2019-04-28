package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireballBrain.MoveState;

public class MarioFireballSpriteFrame extends AnimSpriteFrameInput {
	public MoveState moveState;
	public MarioFireballSpriteFrame(boolean visible, Vector2 position, boolean flipX, float timeDelta,
			MoveState moveState) {
		super(visible, position, flipX, timeDelta);
		this.moveState = moveState;
	}
}
