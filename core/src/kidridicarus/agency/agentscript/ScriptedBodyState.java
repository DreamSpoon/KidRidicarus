package kidridicarus.agency.agentscript;

import com.badlogic.gdx.math.Vector2;

public class ScriptedBodyState {
	public boolean contactEnabled;
	public Vector2 position;
	public float gravityFactor;

	public ScriptedBodyState() {
		contactEnabled = false;	// default no contact
		position = new Vector2(0f, 0f);
		gravityFactor = 1f;	// default regular gravity
	}
}
