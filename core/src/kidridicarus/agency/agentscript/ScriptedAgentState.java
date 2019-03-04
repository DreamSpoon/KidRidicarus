package kidridicarus.agency.agentscript;

/*
 * Basic info about a player agent, that the script can manipulate to achieve an effect.
 *   e.g. Move a sprite across the screen by manipulating the sprite's position over time.
 * Note: The information that this class contains is like a "snapshot" of the agent's state at a point in time.
 *   It can be updated on a frame by frame basis to achieve animation.
 */
public class ScriptedAgentState {
	public ScriptedBodyState scriptedBodyState;
	public ScriptedSpriteState scriptedSpriteState;

	public ScriptedAgentState() {
		scriptedBodyState = new ScriptedBodyState();
		scriptedSpriteState = new ScriptedSpriteState();
	}

	public ScriptedAgentState(ScriptedBodyState scriptedBodyState, ScriptedSpriteState scriptedSpriteState) {
		this.scriptedBodyState = scriptedBodyState;
		this.scriptedSpriteState = scriptedSpriteState;
	}

	public ScriptedAgentState cpy() {
		return new ScriptedAgentState(new ScriptedBodyState(this.scriptedBodyState),
				new ScriptedSpriteState(this.scriptedSpriteState));
	}
}
