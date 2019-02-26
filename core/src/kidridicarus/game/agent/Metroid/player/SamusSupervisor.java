package kidridicarus.game.agent.Metroid.player;

import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.guide.SuperAdvice;
import kidridicarus.game.guide.GameAdvice;

public class SamusSupervisor implements AgentSupervisor {
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

	/*
	 * Advise (pull)
	 */
	@Override
	public GameAdvice pollFrameAdvice() {
		GameAdvice adv = advice.cpy();
		advice.clear();
		return adv;
	}

	/*
	 * TODO: Supervise (push).
	 * (e.g. scripted actions)
	 */
}
