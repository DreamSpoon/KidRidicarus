package kidridicarus.game.agent.SMB.player;

import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.agency.tool.SuperAdvice;
import kidridicarus.game.play.GameAdvice;

public class MarioSupervisor implements AgentSupervisor {
	private Mario mario;
	private GameAdvice advice;

	public MarioSupervisor(Mario mario) {
		this.mario = mario;
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
}
