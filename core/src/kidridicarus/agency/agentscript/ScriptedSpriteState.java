package kidridicarus.agency.agentscript;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.Direction4;

public class ScriptedSpriteState {
	public enum SpriteState { STAND, MOVE }

	public Vector2 position;
	public boolean visible;
	public SpriteState spriteState;
	public boolean facingRight;
	public Direction4 moveDir;

	public ScriptedSpriteState() {
		position = new Vector2(0f, 0f);
		visible = false;
		spriteState = SpriteState.STAND;
		facingRight = false;
		moveDir = null;
	}

	public ScriptedSpriteState(ScriptedSpriteState other) {
		this.position = other.position.cpy();
		this.visible = other.visible;
		this.spriteState = other.spriteState;
		this.facingRight = other.facingRight;
		this.moveDir = other.moveDir;
	}
}
