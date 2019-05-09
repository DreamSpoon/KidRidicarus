package kidridicarus.game.Metroid.agent.player.samus;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.Metroid.agent.player.samus.SamusBrain.MoveState;

public class SamusSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;
	public boolean isDmgFrame;
	public Direction4 climbDir;
	public boolean isFacingUp;

	public SamusSpriteFrameInput(FrameTime frameTime, Vector2 position, MoveState moveState, boolean isFacingRight,
			boolean isFacingUp, boolean isDmgFrame, Direction4 climbDir) {
		super(frameTime, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
		this.isFacingUp = isFacingUp;
		this.isDmgFrame = isDmgFrame;
		this.climbDir = climbDir;
	}
}
