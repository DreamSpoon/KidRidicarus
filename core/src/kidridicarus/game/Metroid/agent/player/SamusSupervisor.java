package kidridicarus.game.Metroid.agent.player;

import kidridicarus.agency.agentscript.AgentScript.AgentScriptHooks;
import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.agency.tool.MoveAdvice;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.game.info.GameKV;

public class SamusSupervisor extends GameAgentSupervisor {
	private MoveAdvice advice;
	private Samus samus;
	private String nextLevelName;

	public SamusSupervisor(Samus samus) {
		advice = new MoveAdvice();
		this.samus = samus;
		nextLevelName = null;
	}

	@Override
	public void setFrameAdvice(MoveAdvice superAdvice) {
		advice.set(superAdvice);
	}

	@Override
	public MoveAdvice pollFrameAdvice() {
		MoveAdvice adv = advice.cpy();
		advice.clear();
		return adv;
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
	protected ScriptedAgentState getCurrentScriptAgentState() {
		ScriptedAgentState curState = new ScriptedAgentState();
		curState.scriptedBodyState.contactEnabled = true;
		curState.scriptedBodyState.position.set(samus.getPosition());

		curState.scriptedSpriteState.position.set(samus.getPosition());
		curState.scriptedSpriteState.visible = true;
		curState.scriptedSpriteState.spriteState =
				samus.getProperty(GameKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		curState.scriptedSpriteState.facingRight = samus.getProperty(GameKV.Script.KEY_FACINGRIGHT, false, Boolean.class);
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
}
