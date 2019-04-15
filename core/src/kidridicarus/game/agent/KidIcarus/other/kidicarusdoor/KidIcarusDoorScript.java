package kidridicarus.game.agent.KidIcarus.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;

public class KidIcarusDoorScript implements AgentScript {
	private ScriptedAgentState currentScriptAgentState;
	private Vector2 exitPos;

	public KidIcarusDoorScript(KidIcarusDoor entranceDoor, Agent exitSpawner) {
		// close the door
		entranceDoor.setOpened(false);
		// save exit position
		exitPos = exitSpawner.getPosition();
	}

	@Override
	public void startScript(Agency agency, AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState) {
		currentScriptAgentState = beginScriptAgentState.cpy();
		// immediately set body position to exit position
		currentScriptAgentState.scriptedBodyState.position.set(exitPos);
	}

	@Override
	public boolean update(float delta) {
		// no updates needed
		return false;
	}

	@Override
	public ScriptedAgentState getScriptAgentState() {
		return this.currentScriptAgentState;
	}

	@Override
	public boolean isOverridable(AgentScript nextScript) {
		return false;
	}
}
