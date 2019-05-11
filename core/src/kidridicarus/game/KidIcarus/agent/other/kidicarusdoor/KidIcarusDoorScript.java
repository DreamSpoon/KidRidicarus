package kidridicarus.game.KidIcarus.agent.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

/*
 * Name: Kid Icarus Door Script
 * Desc: Transport player from one place to another, and close the door behind them.
 */
class KidIcarusDoorScript implements AgentScript {
	private static final float WAIT_TIME_ENTER = 1f;
	private static final float WAIT_TIME_EXIT = 0.75f;

	private enum MoveState { ENTER, EXIT, END }

	private ScriptedAgentState currentScriptAgentState;
	private Vector2 exitPos;
	private float moveStateTimer;
	private MoveState moveState;
	private KidIcarusDoor entranceDoor;
	private Direction4 exitDir;

	KidIcarusDoorScript(KidIcarusDoor entranceDoor, Agent exitSpawner) {
		moveStateTimer = 0f;
		moveState = MoveState.ENTER;
		// save ref to door so door can be "closed" after player enters door
		this.entranceDoor = entranceDoor;
		// save exit position for player exit
		exitPos = AP_Tool.getCenter(exitSpawner);
		if(exitPos == null) {
			throw new IllegalArgumentException("Cannot create Kid Icarus door script from exit spawner " +
					"with no defined position, spawner=" + exitSpawner);
		}
		exitDir = exitSpawner.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
	}

	@Override
	public void startScript(AgentHooks agentHooks, AgentScriptHooks scriptHooks,
			ScriptedAgentState beginScriptAgentState) {
		// error if this script is used more than once - i.e. script restart not allowed
		if(moveState != MoveState.ENTER)
			throw new IllegalStateException("Script restart not allowed.");
		// copy the current state, but disable contacts and movement
		currentScriptAgentState = beginScriptAgentState.cpy();
		currentScriptAgentState.scriptedBodyState.contactEnabled = false;
		currentScriptAgentState.scriptedBodyState.gravityFactor = 0f;
	}

	@Override
	public boolean update(FrameTime frameTime) {
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case ENTER:
				break;
			case EXIT:
				if(isMoveStateChange) {
					// close the door
					entranceDoor.onTakeTrigger();
					// set body and sprite position to to exit position
					currentScriptAgentState.scriptedBodyState.position.set(exitPos);
					currentScriptAgentState.scriptedSpriteState.position.set(exitPos);

					// set player facing direction if the exit spawner has direction property
					if(exitDir == Direction4.RIGHT)
						currentScriptAgentState.scriptedSpriteState.isFacingRight = true;
					else if(exitDir == Direction4.LEFT)
						currentScriptAgentState.scriptedSpriteState.isFacingRight = false;
				}
				break;
			case END:
				if(isMoveStateChange) {
					// re-enable contacts
					currentScriptAgentState.scriptedBodyState.contactEnabled = true;
					currentScriptAgentState.scriptedBodyState.gravityFactor = 1f;
				}
				return false;
		}

		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+frameTime.timeDelta;
		moveState = nextMoveState;
		return true;
	}

	private MoveState getNextMoveState() {
		if(moveState == MoveState.ENTER) {
			if(moveStateTimer > WAIT_TIME_ENTER)
				return MoveState.EXIT;
			else
				return MoveState.ENTER;
		}
		else if(moveState == MoveState.EXIT) {
			if(moveStateTimer > WAIT_TIME_EXIT)
				return MoveState.END;
			else
				return MoveState.EXIT;
		}
		else
			return MoveState.END;
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
