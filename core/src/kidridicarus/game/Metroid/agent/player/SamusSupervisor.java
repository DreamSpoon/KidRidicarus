package kidridicarus.game.Metroid.agent.player;

import kidridicarus.agency.agentscript.ScriptAgentStatus;
import kidridicarus.agency.agentscript.ScriptedSpritState.SpriteState;
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
	protected ScriptAgentStatus getCurrentScriptAgentStatus() {
		ScriptAgentStatus curStatus = new ScriptAgentStatus();
		curStatus.scriptedBodyState.contactEnabled = true;
		curStatus.scriptedBodyState.position.set(samus.getPosition());

		curStatus.scriptedSpriteState.position.set(samus.getPosition());
		curStatus.scriptedSpriteState.visible = true;
		curStatus.scriptedSpriteState.spriteState =
				samus.getProperty(GameKV.Script.KEY_SPRITESTATE, SpriteState.STAND, SpriteState.class);
		curStatus.scriptedSpriteState.facingRight = samus.getProperty(GameKV.Script.KEY_FACINGRIGHT, false, Boolean.class);

		return curStatus;
	}
}
