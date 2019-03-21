package kidridicarus.game.agent.Metroid.player.samus;

import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.MoveAdvice;

public class SamusSupervisor extends GameAgentSupervisor {
	private MoveAdvice moveAdvice;
	private Samus samus;
	private String nextLevelName;
	private boolean isGameOver;

	public SamusSupervisor(Samus samus) {
		moveAdvice = new MoveAdvice();
		this.samus = samus;
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
		ScriptedAgentState scriptedState = new ScriptedAgentState();
		scriptedState.scriptedBodyState.contactEnabled = true;
		scriptedState.scriptedBodyState.position.set(samus.getPosition());
		scriptedState.scriptedSpriteState.visible = true;
		scriptedState.scriptedSpriteState.position.set(samus.getPosition());
		scriptedState.scriptedSpriteState.spriteState =
				samus.getProperty(CommonKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		scriptedState.scriptedSpriteState.facingRight =
				samus.getProperty(CommonKV.Script.KEY_FACINGRIGHT, false, Boolean.class);
		return scriptedState;
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
	public boolean isSwitchToOtherChar() {
		return false;
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
