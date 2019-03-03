package kidridicarus.agency.agentscript;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.tool.Direction4;

public class ScriptedSpritState {
	public enum SpriteState { STAND, MOVE }

	public Vector2 position;
	public boolean visible;
	public SpriteState spriteState;
	public boolean facingRight;
	public Direction4 moveDir;

	public ScriptedSpritState() {
		visible = false;
		position = new Vector2(0f, 0f);
		spriteState = SpriteState.STAND;
		facingRight = false;
		moveDir = null;
	}
}
