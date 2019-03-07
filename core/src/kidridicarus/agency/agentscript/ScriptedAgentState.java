package kidridicarus.agency.agentscript;

import kidridicarus.common.tool.MoveAdvice;

/*
 * Basic info about a player agent, that the script can manipulate to achieve an effect.
 *   e.g. Move a sprite across the screen by manipulating the sprite's position over time.
 * Note: The information that this class contains is like a "snapshot" of the agent's state at a point in time.
 *   It can be updated on a frame by frame basis to achieve animation.
 */
public class ScriptedAgentState {
	public MoveAdvice scriptedMoveAdvice;
	public ScriptedBodyState scriptedBodyState;
	public ScriptedSpriteState scriptedSpriteState;

	public ScriptedAgentState() {
		// Init move advice to null, to signal that move advice is not being given (so use body and sprite
		// state instead).
		scriptedMoveAdvice = null;
		scriptedBodyState = new ScriptedBodyState();
		scriptedSpriteState = new ScriptedSpriteState();
	}

	public ScriptedAgentState(MoveAdvice scriptedMoveAdvice, ScriptedBodyState scriptedBodyState,
			ScriptedSpriteState scriptedSpriteState) {
		this.scriptedMoveAdvice = scriptedMoveAdvice;
		this.scriptedBodyState = scriptedBodyState;
		this.scriptedSpriteState = scriptedSpriteState;
	}

	public ScriptedAgentState cpy() {
		return new ScriptedAgentState(this.scriptedMoveAdvice, new ScriptedBodyState(this.scriptedBodyState),
				new ScriptedSpriteState(this.scriptedSpriteState));
	}
}
