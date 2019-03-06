package kidridicarus.agency.agentscript;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.Direction4;

public class ScriptedSpriteState {
	public enum SpriteState { STAND, MOVE, CLIMB }
	public SpriteState spriteState;

	public Vector2 position;
	public boolean visible;
	public boolean facingRight;
	public Direction4 moveDir;

	public ScriptedSpriteState() {
		spriteState = SpriteState.STAND;
		position = new Vector2(0f, 0f);
		visible = false;
		facingRight = false;
		moveDir = null;
	}

	public ScriptedSpriteState(ScriptedSpriteState other) {
		this.spriteState = other.spriteState;
		this.position = other.position.cpy();
		this.visible = other.visible;
		this.facingRight = other.facingRight;
		this.moveDir = other.moveDir;
	}
}
