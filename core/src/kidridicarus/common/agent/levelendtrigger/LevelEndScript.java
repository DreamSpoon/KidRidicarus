package kidridicarus.common.agent.levelendtrigger;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.tool.FrameTime;

class LevelEndScript implements AgentScript {
	private static final float LEVELEND_WAIT = 4f;

	private LevelEndTrigger parent;
	private AgentScriptHooks scriptHooks;
	private ScriptedAgentState curScriptAgentState;
	private float stateTimer;
	private String nextLevelName;

	LevelEndScript(LevelEndTrigger parent, String nextLevelName) {
		this.parent = parent;
		this.nextLevelName = nextLevelName;
		scriptHooks = null;
		curScriptAgentState = null;
		stateTimer = 0f;
	}

	@Override
	public void startScript(AgentHooks agentHooks, AgentScriptHooks scriptHooks,
			ScriptedAgentState beginScriptAgentState) {
		this.scriptHooks = scriptHooks;
		this.curScriptAgentState = beginScriptAgentState.cpy();

		// disable character contacts and hide the sprite
		curScriptAgentState.scriptedBodyState.contactEnabled = false;
		curScriptAgentState.scriptedBodyState.gravityFactor = 0f;
		curScriptAgentState.scriptedSpriteState.visible = false;

		// hoist the end of level flag
		parent.onTakeTrigger();
	}

	@Override
	public boolean update(FrameTime frameTime) {
		if(stateTimer > LEVELEND_WAIT) {
			scriptHooks.gotoNextLevel(nextLevelName);
			// end script updates
			return false;
		}
		stateTimer += frameTime.timeDelta;
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
