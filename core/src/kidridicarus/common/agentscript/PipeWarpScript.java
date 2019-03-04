package kidridicarus.common.agentscript;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentscript.AgentScript;
import kidridicarus.agency.agentscript.ScriptAgentStatus;
import kidridicarus.agency.tool.Direction4;
import kidridicarus.game.tool.QQ;

/*
 * Inputs needed:
 *   -incoming agent size - so sprite moves completely out of view before body is repositioned
 *   -original position of agent - for start of entry animation (get this from beginAgentStatus)
 *   -x position of horizontal pipe entrance, or y position of vertical pipe entrance
 *     -the edge where the sprite is supposed to disappear
 *     -entry horizon offset (ehOffset)
 *   -Vector2 position of final body position after warp
 *   
 */
public class PipeWarpScript implements AgentScript {
	private ScriptAgentStatus curAgentStatus;
	private Vector2 exitPosition;
	private Direction4 entryDir;
	private float ehOffset;
	private Vector2 incomingAgentSize;

	public PipeWarpScript(Vector2 exitPosition, Direction4 entryDir, float ehOffset, Vector2 incomingAgentSize) {
		this.exitPosition = exitPosition;
		this.entryDir = entryDir;
		this.ehOffset = ehOffset;
		this.incomingAgentSize = new Vector2(incomingAgentSize);
		curAgentStatus = null;
	}

	@Override
	public boolean update(float delta) {
QQ.pr("           ****do the update matey!!!");
		return false;
	}

	@Override
	public void startScript(ScriptAgentStatus beginAgentStatus) {
QQ.pr("set begin status");
		this.curAgentStatus = beginAgentStatus;
		curAgentStatus.scriptedBodyState.position.set(exitPosition);
	}

	@Override
	public ScriptAgentStatus getScriptAgentStatus() {
QQ.pr("get agent status already!!");
		return curAgentStatus;
	}
}
