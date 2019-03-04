package kidridicarus.common.agentscript;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptAgentStatus;
import kidridicarus.common.agent.general.PipeWarpHorizon;

public class PipeWarpScript implements AgentScript {
	private static final float ENTRY_TIME = 1f;
	private static final float EXIT_TIME = ENTRY_TIME;

	private enum ScriptState { ENTRY, WARP, EXIT, PRECOMPLETE, COMPLETE }

	private ScriptAgentStatus beginAgentStatus;
	private ScriptAgentStatus curAgentStatus;
	private Vector2 exitPosition;
	private PipeWarpHorizon entryHorizon;
	private PipeWarpHorizon exitHorizon;
	private Vector2 incomingAgentSize;
	private float stateTimer;
	private ScriptState curScriptState;

	public PipeWarpScript(Vector2 exitPosition, PipeWarpHorizon entryHorizon, PipeWarpHorizon exitHorizon,
			Vector2 incomingAgentSize) {
		this.exitPosition = exitPosition;
		this.entryHorizon = entryHorizon;
		this.exitHorizon = exitHorizon;
		this.incomingAgentSize = new Vector2(incomingAgentSize);
		curAgentStatus = null;
		stateTimer = 0f;
		curScriptState = ScriptState.ENTRY;
	}

	@Override
	public void startScript(ScriptAgentStatus beginAgentStatus) {
		this.beginAgentStatus = beginAgentStatus.cpy();
		this.curAgentStatus = beginAgentStatus.cpy();

		curAgentStatus.scriptedBodyState.contactEnabled = false;
		curAgentStatus.scriptedBodyState.gravityFactor = 0f;
	}

	@Override
	public boolean update(float delta) {
		ScriptState nextScriptState = getNextScriptState();
		switch(nextScriptState) {
			case ENTRY:
				curAgentStatus.scriptedSpriteState.position.set(getSpriteEntryPosition(stateTimer));
				break;
			case WARP:
				curAgentStatus.scriptedBodyState.position.set(getBodyExitPosition());
				break;
			case EXIT:
				if(exitHorizon != null)
					curAgentStatus.scriptedSpriteState.position.set(getSpriteExitPosition(stateTimer));
				break;
			case PRECOMPLETE:
				curAgentStatus.scriptedBodyState.contactEnabled = true;
				curAgentStatus.scriptedBodyState.gravityFactor = 1f;
				break;
			case COMPLETE:
				// end script
				return false;
		}

		stateTimer = curScriptState == nextScriptState ? stateTimer+delta : 0f;
		curScriptState = nextScriptState;
		return true;
	}

	private ScriptState getNextScriptState() {
		if(curScriptState == ScriptState.PRECOMPLETE || curScriptState == ScriptState.COMPLETE)
			return ScriptState.COMPLETE;
		else if (curScriptState == ScriptState.EXIT) {
			if(exitHorizon == null || stateTimer >= EXIT_TIME)
				return ScriptState.PRECOMPLETE;
			else
				return ScriptState.EXIT;
		}
		else if(curScriptState == ScriptState.WARP)
			return ScriptState.EXIT;
		else if(entryHorizon == null || stateTimer >= ENTRY_TIME)
			return ScriptState.WARP;
		else
			return ScriptState.ENTRY;
	}

	private Vector2 getBodyExitPosition() {
		if(exitHorizon == null)
			return exitPosition;
		else {
			switch(exitHorizon.direction) {
				case RIGHT:
					return exitPosition.cpy().add(incomingAgentSize.x/2f, 0f);
				case LEFT:
					return exitPosition.cpy().add(-incomingAgentSize.x/2f, 0f);
				case UP:
					return exitPosition.cpy().add(0f, incomingAgentSize.y/2f);
				default:
					return exitPosition.cpy().add(0f, -incomingAgentSize.y/2f);
			}
		}
	}

	// lerp the sprite position so that the sprite is exactly behind the horizon at finish time
	private Vector2 getSpriteEntryPosition(float time) {
		Vector2 start = beginAgentStatus.scriptedSpriteState.position.cpy();
		Vector2 delta = new Vector2(0f, 0f);
		switch(entryHorizon.direction) {
			case RIGHT:
				delta.x = entryHorizon.bounds.x - start.x + incomingAgentSize.x/2f;
				break;
			case LEFT:
				delta.x = entryHorizon.bounds.x - start.x - incomingAgentSize.x/2f;
				break;
			case UP:
				delta.y = entryHorizon.bounds.y - start.y + incomingAgentSize.y/2f;
				break;
			case DOWN:
				delta.y = entryHorizon.bounds.y - start.y - incomingAgentSize.y/2f;
				break;
		}
		return start.add(delta.scl(time / ENTRY_TIME));
	}

	// lerp the sprite position so that the sprite is exactly ahead of the horizon at finish time
	private Vector2 getSpriteExitPosition(float time) {
		Vector2 delta = new Vector2();
		Vector2 start = new Vector2();
		Rectangle exitBounds = exitHorizon.bounds;
		switch(exitHorizon.direction) {
			case RIGHT:
				start.set(exitBounds.x - incomingAgentSize.x/2f, exitBounds.y + exitBounds.height/2f);
				delta.set(incomingAgentSize.x, 0f);
				break;
			case LEFT:
				start.set(exitBounds.x + incomingAgentSize.x/2f, exitBounds.y + exitBounds.height/2f);
				delta.set(-incomingAgentSize.x, 0f);
				break;
			case UP:
				start.set(exitBounds.x + exitBounds.width/2f, exitBounds.y - incomingAgentSize.y/2f);
				delta.set(0f, incomingAgentSize.y);
				break;
			default:
				start.set(exitBounds.x + exitBounds.width/2f, exitBounds.y + incomingAgentSize.y/2f);
				delta.set(0f, -incomingAgentSize.y);
				break;
		}
		return start.add(delta.scl(time / EXIT_TIME));
	}

	@Override
	public ScriptAgentStatus getScriptAgentStatus() {
		return curAgentStatus;
	}
}
