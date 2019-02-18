package kidridicarus.tool;

import com.badlogic.gdx.Input;

public class KeyboardMapping {
	public static final int DEBUG_TOGGLE = Input.Keys.F1;
	public static final int CHEAT_POWERUP = Input.Keys.F2;
	public static final int FORCE_FRAMERATE_TOGGLE = Input.Keys.F3;
	public static final int FORCE_FRAMERATE_FASTER = Input.Keys.F4;
	public static final int FORCE_FRAMERATE_SLOWER = Input.Keys.F5;

	public static final int MOVE_RIGHT = Input.Keys.D;
	public static final int MOVE_UP = Input.Keys.W;
	public static final int MOVE_LEFT = Input.Keys.A;
	public static final int MOVE_DOWN = Input.Keys.S;
	// run and shoot share a key temporarily
	public static final int MOVE_RUNSHOOT = Input.Keys.LEFT;
	public static final int MOVE_JUMP = Input.Keys.DOWN;
}
