package kidridicarus.agency.agentscript;

/*
 * Basic info about the agent, that the script can manipulate to achieve an effect.
 *   e.g. Move a sprite across the screen by manipulating the sprite's position over time.
 * Note: The information that this class contains is like a "snapshot" of the agent's status at a point in time.
 *   It can be updated on a frame by frame basis to achieve animation.
 */
public class ScriptAgentStatus {
	public ScriptedBodyState scriptedBodyState;
	public ScriptedSpriteState scriptedSpriteState;

	public ScriptAgentStatus() {
		scriptedBodyState = new ScriptedBodyState();
		scriptedSpriteState = new ScriptedSpriteState();
	}

	public ScriptAgentStatus(ScriptedBodyState scriptedBodyState, ScriptedSpriteState scriptedSpriteState) {
		this.scriptedBodyState = scriptedBodyState;
		this.scriptedSpriteState = scriptedSpriteState;
	}

	public ScriptAgentStatus cpy() {
		return new ScriptAgentStatus(new ScriptedBodyState(this.scriptedBodyState),
				new ScriptedSpriteState(this.scriptedSpriteState));
	}
}
