package kidridicarus.game.SMB.agentscript;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.SMB.agent.other.Flagpole;

/*
 * SMB end of level flagpole script.
 * The script ends with character moving right, with expectation that level end will be triggered and
 * level end script will override this script.
 */
public class FlagpoleScript implements AgentScript {
	private static final float SLIDE_SPEED = UInfo.P2M(64f);
	private static final float SLIDE_WAIT_TIME = 0.4f;
	private static final float MOVERIGHT_MAXTIME = 4f;

	private enum ScriptState { SLIDE, SLIDE_STOP, SLIDE_FLIPRIGHT, SLIDE_WAIT, SLIDE_RELEASE, MOVERIGHT, COMPLETE }

	private ScriptedAgentState beginAgentState;
	private ScriptedAgentState curScriptAgentState;
	private Rectangle flagpoleBounds;
	private Vector2 incomingAgentSize;
	private float stateTimer;
	private ScriptState curScriptState;

	private boolean isSlideFinished;
	private float slideDuration;

	public FlagpoleScript(Rectangle flagpoleBounds, Vector2 incomingAgentSize) {
		this.flagpoleBounds = flagpoleBounds;
		this.incomingAgentSize = incomingAgentSize.cpy();
		beginAgentState = null;
		curScriptAgentState = null;
		isSlideFinished = false;
		slideDuration = 0f;
		stateTimer = 0f;
		curScriptState = ScriptState.SLIDE;
	}

	@Override
	public void startScript(AgentScriptHooks asHooks, ScriptedAgentState beginScriptAgentState) {
		this.beginAgentState = beginScriptAgentState.cpy();
		this.curScriptAgentState = beginScriptAgentState.cpy();

		// Disable contacts so body won't interact with other agents, and disable gravity so the body
		// doesn't fall out of the level. 
		curScriptAgentState.scriptedBodyState.contactEnabled = false;
		curScriptAgentState.scriptedBodyState.gravityFactor = 0f;

		curScriptAgentState.scriptedSpriteState.facingRight = true;
		// show climb down sprite
		curScriptAgentState.scriptedSpriteState.spriteState = SpriteState.CLIMB;
		curScriptAgentState.scriptedSpriteState.moveDir = Direction4.DOWN;
	}

	@Override
	public boolean update(float delta) {
		ScriptState nextScriptState = getNextScriptState();
		switch(nextScriptState) {
			// sprite sliding down flagpole
			case SLIDE:
				curScriptAgentState.scriptedSpriteState.position.set(getSpriteSlidePosition(stateTimer));
				isSlideFinished = isAgentAtBottom(stateTimer);
				break;
			case SLIDE_STOP:
				if(curScriptState != ScriptState.SLIDE_STOP) {
					slideDuration = stateTimer;
					curScriptAgentState.scriptedSpriteState.moveDir = null;
				}
				break;
			case SLIDE_FLIPRIGHT:
				curScriptAgentState.scriptedSpriteState.facingRight = false;
				curScriptAgentState.scriptedSpriteState.position.set(getSlideEndRightPosition());
				break;
			case SLIDE_WAIT:
				break;
			case SLIDE_RELEASE:
				curScriptAgentState.scriptedSpriteState.spriteState = SpriteState.STAND;
				curScriptAgentState.scriptedBodyState.position.set(getBodyExitPosition());
				// contacts and gravity were disabled at the start of script, so re-enable here
				curScriptAgentState.scriptedBodyState.contactEnabled = true;
				curScriptAgentState.scriptedBodyState.gravityFactor = 1f;
				break;
			case MOVERIGHT:
				// if first frame of this state then start character moving right
				if(curScriptState != nextScriptState) {
					curScriptAgentState.scriptedMoveAdvice = new MoveAdvice();
					curScriptAgentState.scriptedMoveAdvice.moveRight = true;
				}
				break;
			case COMPLETE:
				// Character will walk right towards the level end trigger, and level end script will start when
				// level end is contacted by character
				return false;
		}

		stateTimer = curScriptState == nextScriptState ? stateTimer+delta : 0f;
		curScriptState = nextScriptState;
		return true;
	}

	private ScriptState getNextScriptState() {
		switch(curScriptState) {
			case SLIDE:
				if(isSlideFinished)
					return ScriptState.SLIDE_STOP;
				return ScriptState.SLIDE;
			case SLIDE_STOP:
				if(stateTimer > Flagpole.FLAGDROP_TIME - slideDuration)
					return ScriptState.SLIDE_FLIPRIGHT;
				return ScriptState.SLIDE_STOP;
			case SLIDE_FLIPRIGHT:
				return ScriptState.SLIDE_WAIT;
			case SLIDE_WAIT:
				if(stateTimer > SLIDE_WAIT_TIME)
					return ScriptState.SLIDE_RELEASE;
				return ScriptState.SLIDE_WAIT;
			case SLIDE_RELEASE:
				return ScriptState.MOVERIGHT;
			case MOVERIGHT:
				if(stateTimer > MOVERIGHT_MAXTIME)
					return ScriptState.COMPLETE;
				return ScriptState.MOVERIGHT;
			case COMPLETE:
			default:
				return ScriptState.COMPLETE;
		}
	}

	private Vector2 getSpriteSlidePosition(float time) {
		// position sprite just to the left of the flagpole
		return new Vector2(flagpoleBounds.x+flagpoleBounds.width/2f - incomingAgentSize.x/2f, getAgentYforTime(time));
	}

	private float getAgentYforTime(float time) {
		// start Y is equal to beginning Y clamped to flagpole vertical bounds
		float startY = clamp(beginAgentState.scriptedBodyState.position.y, flagpoleBounds.y,
				flagpoleBounds.y+flagpoleBounds.height);

		float currentY = startY - SLIDE_SPEED*time;
		// clamp min Y value to the end position at bottom of flagpole
		float endY = getAgentYforSlideEnd(); 
		if(currentY < endY)
			return endY;
		return currentY;
	}

	private float getAgentYforSlideEnd() {
		return flagpoleBounds.y + incomingAgentSize.y/2f;
	}

	private float clamp(float x, float min, float max) {
		if(x < min)
			return min;
		else if(x > max)
			return max;
		return x;
	}

	private Vector2 getBodyExitPosition() {
		return new Vector2(flagpoleBounds.x + flagpoleBounds.width + incomingAgentSize.x,
				flagpoleBounds.y + incomingAgentSize.y/2f);
	}

	private boolean isAgentAtBottom(float time) {
		return getAgentYforTime(time) == getAgentYforSlideEnd();
	}

	private Vector2 getSlideEndRightPosition() {
		return new Vector2(flagpoleBounds.x+flagpoleBounds.width/2f + incomingAgentSize.x/2f,
				getAgentYforSlideEnd());
	}

	@Override
	public ScriptedAgentState getScriptAgentState() {
		return curScriptAgentState;
	}

	@Override
	public boolean isOverridable() {
		// override allowed if script is in final states
		if(curScriptState == ScriptState.MOVERIGHT || curScriptState == ScriptState.COMPLETE)
			return true;
		return false;
	}
}
