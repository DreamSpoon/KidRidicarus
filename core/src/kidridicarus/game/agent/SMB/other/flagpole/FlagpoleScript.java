package kidridicarus.game.agent.SMB.other.flagpole;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;

/*
 * SMB end of level flagpole script.
 * The script ends with character moving right, with expectation that level end will be triggered and
 * level end script will override this script.
 */
public class FlagpoleScript implements AgentScript {
	private static final float SLIDE_SPEED = UInfo.P2M(96f);
	private static final float SLIDE_WAIT_TIME = 0.4f;
	private static final float MOVERIGHT_MAXTIME = 4f;

	private enum ScriptState { SLIDE, SLIDE_STOP, SLIDE_FLIPRIGHT, SLIDE_WAIT, SLIDE_RELEASE, MOVERIGHT, COMPLETE }

	private ScriptState curScriptState;
	private float stateTimer;
	private ScriptedAgentState beginScriptedState;
	private ScriptedAgentState scriptedState;
	private Flagpole parent;
	private Vector2 playerAgentSize;

	private boolean isSlideFinished;
	private float slideDuration;

	public FlagpoleScript(Flagpole parent, Vector2 incomingAgentSize) {
		this.parent = parent;
		this.playerAgentSize = incomingAgentSize.cpy();
		beginScriptedState = null;
		scriptedState = null;
		isSlideFinished = false;
		slideDuration = 0f;
		stateTimer = 0f;
		curScriptState = ScriptState.SLIDE;
	}

	@Override
	public void startScript(AgentScriptHooks asHooks, ScriptedAgentState beginAgentState) {
		this.beginScriptedState = beginAgentState.cpy();
		this.scriptedState = beginAgentState.cpy();

		// Disable contacts so body won't interact with other agents, and disable gravity so the body
		// doesn't fall out of the level. 
		scriptedState.scriptedBodyState.contactEnabled = false;
		scriptedState.scriptedBodyState.gravityFactor = 0f;

		scriptedState.scriptedSpriteState.isFacingRight = true;
		// show climb down sprite
		scriptedState.scriptedSpriteState.spriteState = SpriteState.CLIMB;
		scriptedState.scriptedSpriteState.moveDir = Direction4.DOWN;

		// trigger the flag drop
		parent.onTakeTrigger();
	}

	@Override
	public boolean update(float delta) {
		ScriptState nextScriptState = getNextScriptState();
		boolean scriptStateChanged = nextScriptState != curScriptState;
		switch(nextScriptState) {
			// sprite sliding down flagpole
			case SLIDE:
				scriptedState.scriptedSpriteState.position.set(getSpriteSlidePosition(stateTimer));
				isSlideFinished = isAgentAtBottom(stateTimer);
				break;
			case SLIDE_STOP:
				if(scriptStateChanged) {
					slideDuration = stateTimer;
					scriptedState.scriptedSpriteState.moveDir = Direction4.NONE;
				}
				break;
			case SLIDE_FLIPRIGHT:
				scriptedState.scriptedSpriteState.isFacingRight = false;
				scriptedState.scriptedSpriteState.position.set(getSlideEndRightPosition());
				break;
			case SLIDE_WAIT:
				break;
			case SLIDE_RELEASE:
				scriptedState.scriptedSpriteState.spriteState = SpriteState.STAND;
				scriptedState.scriptedBodyState.position.set(getBodyExitPosition());
				// contacts and gravity were disabled at the start of script, so re-enable here
				scriptedState.scriptedBodyState.contactEnabled = true;
				scriptedState.scriptedBodyState.gravityFactor = 1f;
				break;
			case MOVERIGHT:
				// if first frame of this state then start character moving right
				if(scriptStateChanged) {
					scriptedState.scriptedMoveAdvice = new MoveAdvice();
					scriptedState.scriptedMoveAdvice.moveRight = true;
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
		return new Vector2(parent.getBounds().x+parent.getBounds().width/2f - playerAgentSize.x/2f,
				getAgentYforTime(time));
	}

	private float getAgentYforTime(float time) {
		// start Y is equal to beginning Y clamped to flagpole vertical bounds
		float startY = clamp(beginScriptedState.scriptedBodyState.position.y, parent.getBounds().y,
				parent.getBounds().y+parent.getBounds().height);

		float currentY = startY - SLIDE_SPEED*time;
		// clamp min Y value to the end position at bottom of flagpole
		float endY = getAgentYforSlideEnd(); 
		if(currentY < endY)
			return endY;
		return currentY;
	}

	private float getAgentYforSlideEnd() {
		return parent.getBounds().y + playerAgentSize.y/2f;
	}

	private float clamp(float x, float min, float max) {
		if(x < min)
			return min;
		else if(x > max)
			return max;
		return x;
	}

	private Vector2 getBodyExitPosition() {
		return new Vector2(parent.getBounds().x + parent.getBounds().width + playerAgentSize.x,
				parent.getBounds().y + playerAgentSize.y/2f);
	}

	private boolean isAgentAtBottom(float time) {
		return getAgentYforTime(time) == getAgentYforSlideEnd();
	}

	private Vector2 getSlideEndRightPosition() {
		return new Vector2(parent.getBounds().x+parent.getBounds().width/2f + playerAgentSize.x/2f,
				getAgentYforSlideEnd());
	}

	@Override
	public ScriptedAgentState getScriptAgentState() {
		return scriptedState;
	}

	@Override
	public boolean isOverridable(AgentScript nextScript) {
		// override allowed if script is in final states
		return curScriptState == ScriptState.MOVERIGHT || curScriptState == ScriptState.COMPLETE;
	}
}
