package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireballBrain.MoveState;

public class MarioFireballSpriteFrame extends SpriteFrameInput {
	public MoveState moveState;

	public MarioFireballSpriteFrame(Vector2 position, boolean isFacingRight, FrameTime frameTime, MoveState moveState) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
