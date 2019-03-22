package kidridicarus.common.agent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.AgentScriptRunner;

/*
 * Supervisor is expected to handle stuff for PlayerAgents:
 *   -scripted actions
 *   -relaying advice to the agent
 *   -...
 */
public abstract class AgentSupervisor {
	protected Agency agency;
	private AgentScriptRunner scriptRunner;
	protected Agent playerAgent;
	private RoomBox currentRoom;
	private Vector2 lastViewCenter;

	public abstract void setMoveAdvice(MoveAdvice moveAdvice);
	// internalPollMoveAdvice method to be implemented by superclass, for use by this class only.
	// Other classes that poll move advice from a superclass must call pollMoveAdvice method.
	protected abstract MoveAdvice internalPollMoveAdvice();
	public abstract boolean isAtLevelEnd();
	public abstract String getNextLevelFilename();
	public abstract boolean isGameOver();
	public abstract void setStageHUD(Stage stageHUD);
	public abstract void drawHUD();
	public abstract boolean isSwitchToOtherChar();

	/*
	 * Convert the Player agent state information into a simpler script agent state format, and return it.
	 * Script agent state is used to initialize/direct the agent state when a script is started/running.
	 */
	protected abstract ScriptedAgentState getCurrentScriptAgentState();

	protected abstract AgentScriptHooks getAgentScriptHooks();

	public AgentSupervisor(Agency agency, Agent agent) {
		this.agency = agency;
		scriptRunner = new AgentScriptRunner(agency);
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);
		this.playerAgent = agent;
		currentRoom = null;
		lastViewCenter = new Vector2(0f, 0f);
	}

	public void preUpdateAgency(float delta) {
		scriptRunner.preUpdateAgency(delta);
	}

	/*
	 * Postupdate the scriptrunner, and check for room changes; which may lead to view changes, music changes, etc.
	 */
	public void postUpdateAgency() {
		scriptRunner.postUpdateAgency();

		// check if player changed room, and if so, did the room music change?
		RoomBox nextRoom = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(currentRoom != nextRoom) {
			roomChange(nextRoom);
			currentRoom = nextRoom;
		}
	}

	// return false if already running a script, otherwise start using the given script and return true 
	public boolean startScript(AgentScript agentScript) {
		return scriptRunner.startScript(agentScript, getAgentScriptHooks(), getCurrentScriptAgentState());
	}

	public ScriptedAgentState getScriptAgentState() {
		return scriptRunner.getScriptAgentState();
	}

	public boolean isRunningScript() {
		return scriptRunner.isRunning();
	}

	public boolean isRunningScriptMoveAdvice() {
		return scriptRunner.isRunningMoveAdvice();
	}

	public boolean isRunningScriptNoMoveAdvice() {
		return scriptRunner.isRunning() && !scriptRunner.isRunningMoveAdvice();
	}

	/*
	 * Returns scripted move advice if scripted move advice script is running.
	 * Otherwise returns regular user move advice.
	 */
	public MoveAdvice pollMoveAdvice() {
		if(scriptRunner.isRunningMoveAdvice())
			return scriptRunner.getScriptAgentState().scriptedMoveAdvice;
		else
			return internalPollMoveAdvice();
	}
	/*
	 * Check current room to get view center, and retain last known view center if room becomes null.
	 */
	public Vector2 getViewCenter() {
		RoomBox room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room == null)
			return lastViewCenter;
		lastViewCenter.set(((PlayerAgent) playerAgent).getCurrentRoom().getViewCenterForPos(
				playerAgent.getPosition()));
		return lastViewCenter;
	}

	public void roomChange(RoomBox newRoom) {
		if(newRoom != null)
			agency.getEar().onChangeAndStartMainMusic(newRoom.getRoommusic());
	}
}
