package kidridicarus.game.agent.Metroid.player;

import kidridicarus.agency.tool.SuperAdvice;
import kidridicarus.game.agent.GameAgentSupervisor;
import kidridicarus.game.play.GameAdvice;

public class SamusSupervisor implements GameAgentSupervisor {
	private Samus samus;
	private GameAdvice advice;

	public SamusSupervisor(Samus samus) {
		this.samus = samus;
		advice = new GameAdvice();
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
}
