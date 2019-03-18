package kidridicarus.game.agent.SMB.other.pipewarp;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp.PipeWarpHorizon;

/*
 * Pipe Warp Script:
 * 1) Animate agent sprite entering pipe warp.
 * 2) Warp the agent body to pipe warp exit position.
 * 3) Animate the agent sprite exiting a pipe warp (or the agent immediately appears at the non-pipe warp exit).
 */
public class PipeWarpScript implements AgentScript {
	private static final float ENTRY_TIME = 1f;
	private static final float EXIT_TIME = ENTRY_TIME;

	private enum ScriptState { ENTRY, WARP, EXIT, COMPLETE }

	private Vector2 exitPosition;
	private PipeWarpHorizon entryHorizon;
	private PipeWarpHorizon exitHorizon;
	private Vector2 incomingAgentSize;
	private ScriptedAgentState beginAgentState;
	private ScriptedAgentState curScriptAgentState;
	private float stateTimer;
	private ScriptState curScriptState;

	public PipeWarpScript(Vector2 exitPosition, PipeWarpHorizon entryHorizon, PipeWarpHorizon exitHorizon,
			Vector2 incomingAgentSize) {
		this.exitPosition = exitPosition;
		this.entryHorizon = entryHorizon;
		this.exitHorizon = exitHorizon;
		this.incomingAgentSize = incomingAgentSize.cpy();
		beginAgentState = null;
		curScriptAgentState = null;
		stateTimer = 0f;
		curScriptState = ScriptState.ENTRY;
	}

	@Override
	public void startScript(AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState) {
		this.beginAgentState = beginScriptAgentState.cpy();
		this.curScriptAgentState = beginScriptAgentState.cpy();

		// Disable contacts so body won't interact with other agents while warping, and
		// disable gravity so the body doesn't fall out of the level. 
		curScriptAgentState.scriptedBodyState.contactEnabled = false;
		curScriptAgentState.scriptedBodyState.gravityFactor = 0f;
	}

	@Override
	public boolean update(float delta) {
		ScriptState nextScriptState = getNextScriptState();
		switch(nextScriptState) {
			// sprite entering pipe
			case ENTRY:
				curScriptAgentState.scriptedSpriteState.position.set(getSpriteEntryPosition(stateTimer));
				if(entryHorizon.direction.isHorizontal())
					curScriptAgentState.scriptedSpriteState.spriteState = SpriteState.MOVE;
				else
					curScriptAgentState.scriptedSpriteState.spriteState = SpriteState.STAND;
				break;
			// body position warp
			case WARP:
				curScriptAgentState.scriptedBodyState.position.set(getBodyExitPosition());
				break;
			// sprite leaving pipe
			case EXIT:
				if(exitHorizon != null) {
					curScriptAgentState.scriptedSpriteState.position.set(getSpriteExitPosition(stateTimer));
					if(exitHorizon.direction.isHorizontal())
						curScriptAgentState.scriptedSpriteState.spriteState = SpriteState.MOVE;
					else
						curScriptAgentState.scriptedSpriteState.spriteState = SpriteState.STAND;
				}
				break;
			// script complete, return false to end script
			case COMPLETE:
				// contacts and gravity were disabled at the start of script, so re-enable here
				curScriptAgentState.scriptedBodyState.contactEnabled = true;
				curScriptAgentState.scriptedBodyState.gravityFactor = 1f;
				return false;
		}

		stateTimer = curScriptState == nextScriptState ? stateTimer+delta : 0f;
		curScriptState = nextScriptState;
		return true;
	}

	private ScriptState getNextScriptState() {
		if(curScriptState == ScriptState.COMPLETE)
			return ScriptState.COMPLETE;
		else if(curScriptState == ScriptState.EXIT) {
			if(exitHorizon == null || stateTimer >= EXIT_TIME)
				return ScriptState.COMPLETE;
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

	// where to position the body when the warp exit completes? 
	private Vector2 getBodyExitPosition() {
		// if no exit horizon then return exit position with no offset
		if(exitHorizon == null)
			return exitPosition;
		// return the exit position with offset to place body at outer edge of horizon
		else {
			switch(exitHorizon.direction) {
				case RIGHT:
					return new Vector2(exitHorizon.bounds.x, exitHorizon.bounds.y+exitHorizon.bounds.height/2f).
							add(incomingAgentSize.x/2f, 0f);
				case LEFT:
					return new Vector2(exitHorizon.bounds.x, exitHorizon.bounds.y+exitHorizon.bounds.height/2f).
							add(-incomingAgentSize.x/2f, 0f);
				case UP:
					return new Vector2(exitHorizon.bounds.x+exitHorizon.bounds.width/2f, exitHorizon.bounds.y).
							add(0f, incomingAgentSize.y/2f);
				default:
					return new Vector2(exitHorizon.bounds.x+exitHorizon.bounds.width/2f, exitHorizon.bounds.y).
							add(0f, -incomingAgentSize.y/2f);
			}
		}
	}

	// lerp the sprite position so that the sprite is exactly behind the horizon at finish time
	private Vector2 getSpriteEntryPosition(float time) {
		Vector2 start = beginAgentState.scriptedSpriteState.position.cpy();
		Vector2 delta = new Vector2(0f, 0f);
		switch(entryHorizon.direction) {
			case RIGHT:
				delta.x = entryHorizon.bounds.x - start.x + incomingAgentSize.x/2f;
				break;
			case LEFT:
				delta.x = entryHorizon.bounds.x - start.x - incomingAgentSize.x/2f;
				break;
			case UP:
			case NONE:
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
	public ScriptedAgentState getScriptAgentState() {
		return curScriptAgentState;
	}

	@Override
	public boolean isOverridable() {
		return false;
	}
}
