package kidridicarus.game.KidIcarus.agent.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit.MoveState;

public class PitSpriteFrameInput extends AnimSpriteFrameInput {
	public MoveState moveState;
	public boolean isDmgFrame;
	public boolean isShooting;
	public boolean isHeadInTile;
	public boolean isJumpUp;
	public Direction4 climbDir;

	public PitSpriteFrameInput(boolean visible, Vector2 position, boolean isFacingRight, float timeDelta,
			MoveState moveState, boolean isDmgFrame, boolean isShooting, boolean isHeadInTile, boolean isJumpUp,
			Direction4 climbDir) {
		super(visible, position, isFacingRight, timeDelta);
		this.moveState = moveState;
	}
}
