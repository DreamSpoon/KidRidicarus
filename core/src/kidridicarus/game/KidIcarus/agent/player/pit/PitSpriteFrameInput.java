package kidridicarus.game.KidIcarus.agent.player.pit;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit.MoveState;

public class PitSpriteFrameInput extends SpriteFrameInput {
	public MoveState moveState;
	public boolean isDmgFrame;
	public boolean isShooting;
	public boolean isHeadInTile;
	public boolean isJumpUp;
	public Direction4 climbDir;

	public PitSpriteFrameInput(Vector2 position, boolean isFacingRight, float timeDelta, MoveState moveState,
			boolean isDmgFrame, boolean isShooting, boolean isHeadInTile, boolean isJumpUp, Direction4 climbDir) {
		super(false, timeDelta, !isFacingRight, false, 0f, position);
		this.moveState = moveState;
		this.isDmgFrame = isDmgFrame;
		this.isShooting = isShooting;
		this.isHeadInTile = isHeadInTile;
		this.isJumpUp = isJumpUp;
		this.climbDir = climbDir;
	}
}
