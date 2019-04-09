package kidridicarus.game.agent.Metroid.player.samus;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.playeragent.PlayerAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;

public class SamusSupervisor extends PlayerAgentSupervisor {
	private MoveAdvice moveAdvice;
	private String nextLevelName;
	private boolean isGameOver;

	public SamusSupervisor(Agency agency, Samus playerAgent) {
		super(agency, playerAgent);

		moveAdvice = new MoveAdvice();
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice moveAdvice) {
		this.moveAdvice.set(moveAdvice);
	}

	@Override
	protected MoveAdvice internalPollMoveAdvice() {
		MoveAdvice userAdvice = moveAdvice.cpy();
		moveAdvice.clear();
		return userAdvice;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(playerAgent.getPosition());
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.position.set(playerAgent.getPosition());
		curState.scriptedSpriteState.spriteState =
				playerAgent.getProperty(CommonKV.Script.KEY_SPRITE_STATE, SpriteState.STAND, SpriteState.class);
		if(playerAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class) == Direction4.RIGHT)
			curState.scriptedSpriteState.isFacingRight = true;
		else
			curState.scriptedSpriteState.isFacingRight = false;
		return curState;
	}

	@Override
	protected AgentScriptHooks getAgentScriptHooks() {
		return new AgentScriptHooks() {
				@Override
				public void gotoNextLevel(String name) {
					nextLevelName = name;
				}
			};
	}

	@Override
	public String getNextLevelFilename() {
		return nextLevelName;
	}

	public void setGameOver() {
		isGameOver = true;
	}

	@Override
	public boolean isAtLevelEnd() {
		return nextLevelName != null;
	}

	@Override
	public boolean isGameOver() {
		return isGameOver;
	}
}
