package kidridicarus.game.SMB1.agent.player.mario;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.SMB1.agent.player.mario.MarioBrain.MoveState;
import kidridicarus.game.SMB1.agent.player.mario.MarioBrain.PowerState;

class MarioSpriteFrameInput extends SpriteFrameInput {
	MoveState moveState;
	PowerState powerState;
	boolean isDmgFrame;
	boolean isStarPowered;
	boolean didShootFireball;
	Direction4 climbDir;

	MarioSpriteFrameInput(FrameTime frameTime, Vector2 position, MoveState moveState, PowerState powerState,
			boolean isFacingRight, boolean isDmgFrame, boolean isStarPowered, boolean didShootFireball,
			Direction4 climbDir) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
		this.powerState = powerState;
		this.isDmgFrame = isDmgFrame;
		this.isStarPowered = isStarPowered;
		this.didShootFireball = didShootFireball;
		this.climbDir = climbDir;
	}
}
