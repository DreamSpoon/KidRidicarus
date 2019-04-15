package kidridicarus.game.agent.KidIcarus.other.kidicarusdoor;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;

/*
 * Name: Kid Icarus Door Script
 * Desc: Transport player from one place to another, and close the door behind them.
 */
public class KidIcarusDoorScript implements AgentScript {
	private static final float WAIT_TIME_ENTER = 1f;
	private static final float WAIT_TIME_EXIT = 0.75f;

	private enum MoveState { ENTER, EXIT, END }

	private ScriptedAgentState currentScriptAgentState;
	private Vector2 exitPos;
	private float moveStateTimer;
	private MoveState moveState;
	private KidIcarusDoor entranceDoor;
	private Direction4 exitDir;

	public KidIcarusDoorScript(KidIcarusDoor entranceDoor, Agent exitSpawner) {
		moveStateTimer = 0f;
		moveState = MoveState.ENTER;
		// save ref to door so door can be "closed" after player enters door
		this.entranceDoor = entranceDoor;
		// save exit position for player exit
		exitPos = exitSpawner.getPosition();
		exitDir = exitSpawner.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
	}

	@Override
	public void startScript(Agency agency, AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState) {
		// error if this script is used more than once - i.e. script restart not allowed
		if(moveState != MoveState.ENTER)
			throw new IllegalStateException("Script restart not allowed.");
		// copy the current state, but disable contacts and movement
		currentScriptAgentState = beginScriptAgentState.cpy();
		currentScriptAgentState.scriptedBodyState.contactEnabled = false;
		currentScriptAgentState.scriptedBodyState.gravityFactor = 0f;
	}

	@Override
	public boolean update(float delta) {
		MoveState nextMoveState = getNextMoveState();
		boolean isMoveStateChange = nextMoveState != moveState;
		switch(nextMoveState) {
			case ENTER:
				break;
			case EXIT:
				if(isMoveStateChange) {
					// close the door
					entranceDoor.setOpened(false);
					// set body and sprite position to to exit position
					currentScriptAgentState.scriptedBodyState.position.set(exitPos);
					currentScriptAgentState.scriptedSpriteState.position.set(exitPos);

					// set player facing direction if the exit spawner has direction property
					if(exitDir.isRight())
						currentScriptAgentState.scriptedSpriteState.isFacingRight = true;
					else if(exitDir.isLeft())
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

		moveStateTimer = isMoveStateChange ? 0f : moveStateTimer+delta;
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
