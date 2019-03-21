package kidridicarus.agency.agentscript;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.tool.Direction4;

public class ScriptedSpriteState {
	public enum SpriteState { STAND, MOVE, CLIMB }
	public SpriteState spriteState;

	public Vector2 position;
	public boolean visible;
	public boolean isFacingRight;
	public Direction4 moveDir;

	public ScriptedSpriteState() {
		spriteState = SpriteState.STAND;
		position = new Vector2(0f, 0f);
		visible = false;
		isFacingRight = false;
		moveDir = Direction4.NONE;
	}

	public ScriptedSpriteState(ScriptedSpriteState other) {
		this.spriteState = other.spriteState;
		this.position = other.position.cpy();
		this.visible = other.visible;
		this.isFacingRight = other.isFacingRight;
		this.moveDir = other.moveDir;
	}
}
