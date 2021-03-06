package kidridicarus.game.SMB1.agent.player.mariofireball;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireballBrain.MoveState;

class MarioFireballSpriteFrame extends SpriteFrameInput {
	MoveState moveState;

	MarioFireballSpriteFrame(Vector2 position, boolean isFacingRight, FrameTime frameTime, MoveState moveState) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
	}
}
