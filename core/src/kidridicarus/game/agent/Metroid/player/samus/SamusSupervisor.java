package kidridicarus.game.agent.Metroid.player.samus;

import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.MoveAdvice;

public class SamusSupervisor extends GameAgentSupervisor {
	private MoveAdvice curMoveAdvice;
	private Samus samus;
	private String nextLevelName;
	private boolean isGameOver;

	public SamusSupervisor(Samus samus) {
		curMoveAdvice = new MoveAdvice();
		this.samus = samus;
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice moveAdvice) {
		curMoveAdvice.set(moveAdvice);
	}

	@Override
	public MoveAdvice pollMoveAdvice() {
		MoveAdvice adv = curMoveAdvice.cpy();
		curMoveAdvice.clear();
		return adv;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(samus.getPosition());

		curState.scriptedSpriteState.position.set(samus.getPosition());
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.spriteState =
				samus.getProperty(CommonKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		curState.scriptedSpriteState.facingRight = samus.getProperty(CommonKV.Script.KEY_FACINGRIGHT, false,
				Boolean.class);
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
	public boolean isSwitchToOtherChar() {
		return false;
	}

	@Override
	public String getNextLevelName() {
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
