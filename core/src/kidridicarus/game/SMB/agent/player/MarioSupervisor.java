package kidridicarus.game.SMB.agent.player;

import kidridicarus.agency.agentscript.ScriptAgentStatus;
import kidridicarus.agency.tool.SuperAdvice;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.play.GameAdvice;

public class MarioSupervisor extends GameAgentSupervisor {
	private GameAdvice advice;
	private boolean switchToOtherChar;

	public MarioSupervisor(Mario mario) {
		advice = new GameAdvice();
		switchToOtherChar = false;
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

	/*
	 * This should only be called with non-mario powerups TODO: fix this!
	 */
	public void applyNonMarioPowerup(PowType powerupRec) {
		switchToOtherChar = true;
	}

	@Override
	public boolean isSwitchToOtherChar() {
		return switchToOtherChar;
	}

	@Override
	protected ScriptAgentStatus getCurrentScriptAgentStatus() {
		return null;
	}
}
