package kidridicarus.game.agent.SMB.player.luigi;

import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.MoveAdvice;

public class LuigiSupervisor extends GameAgentSupervisor {
	private Luigi luigi;
	private MoveAdvice userMoveAdvice;
	private String nextLevelName;
	private boolean isGameOver;

	public LuigiSupervisor(Luigi luigi) {
		this.luigi = luigi;
		userMoveAdvice = new MoveAdvice();
		nextLevelName = null;
		isGameOver = false;
	}

	@Override
	public void setMoveAdvice(MoveAdvice moveAdvice) {
		userMoveAdvice.set(moveAdvice);
	}

	@Override
	public MoveAdvice pollUserMoveAdvice() {
		MoveAdvice adv = userMoveAdvice.cpy();
		userMoveAdvice.clear();
		return adv;
	}

	@Override
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(luigi.getPosition());

		curState.scriptedSpriteState.position.set(luigi.getPosition());
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.spriteState =
				luigi.getProperty(CommonKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		curState.scriptedSpriteState.facingRight = luigi.getProperty(CommonKV.Script.KEY_FACINGRIGHT, false,
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

	@Override
	public boolean isAtLevelEnd() {
		return nextLevelName != null;
	}

	public void setGameOver() {
		this.isGameOver = true;
	}

	@Override
	public boolean isGameOver() {
		return isGameOver;
	}
}
