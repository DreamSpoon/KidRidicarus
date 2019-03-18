package kidridicarus.game.agent.SMB.other.levelendtrigger;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;

public class LevelEndScript implements AgentScript {
	private static final float LEVELEND_WAIT = 1f;

	private LevelEndTrigger parent;
	private AgentScriptHooks asHooks;
	private ScriptedAgentState curScriptAgentState;
	private float stateTimer;
	private String nextLevelName;

	public LevelEndScript(LevelEndTrigger parent, String nextLevelName) {
		this.parent = parent;
		this.nextLevelName = nextLevelName;
		asHooks = null;
		curScriptAgentState = null;
		stateTimer = 0f;
	}

	@Override
	public void startScript(AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState) {
		this.asHooks = asHooks;
		this.curScriptAgentState = beginScriptAgentState.cpy();

		// disable character contacts and hide the sprite
		curScriptAgentState.scriptedBodyState.contactEnabled = false;
		curScriptAgentState.scriptedBodyState.gravityFactor = 0f;
		curScriptAgentState.scriptedSpriteState.visible = false;

		// hoist the end of level flag
		parent.onTakeTrigger();
	}

	@Override
	public boolean update(float delta) {
		if(stateTimer > LEVELEND_WAIT) {
			asHooks.gotoNextLevel(nextLevelName);
			// end script updates
			return false;
		}
		stateTimer += delta;
		return true;
	}

	@Override
	public ScriptedAgentState getScriptAgentState() {
		return curScriptAgentState;
	}

	@Override
	public boolean isOverridable(AgentScript nextScript) {
		return false;
	}
}
