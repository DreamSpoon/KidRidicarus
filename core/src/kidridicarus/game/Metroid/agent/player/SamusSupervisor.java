package kidridicarus.game.Metroid.agent.player;

import kidridicarus.agency.agentscript.ScriptedAgentState;
import kidridicarus.agency.agentscript.ScriptedSpriteState.SpriteState;
import kidridicarus.agency.tool.SuperAdvice;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.play.GameAdvice;

public class SamusSupervisor extends GameAgentSupervisor {
	private GameAdvice advice;
	private Samus samus;

	public SamusSupervisor(Samus samus) {
		advice = new GameAdvice();
		this.samus = samus;
	}

	@Override
	public void setFrameAdvice(SuperAdvice superAdvice) {
		advice.fromSuperAdvice(superAdvice);
	}

	@Override
	public GameAdvice pollFrameAdvice() {
		GameAdvice adv = advice.cpy();
		advice.clear();
		return adv;
	}

	@Override
	public boolean isSwitchToOtherChar() {
		return false;
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
}
