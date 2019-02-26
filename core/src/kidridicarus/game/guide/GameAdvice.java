package kidridicarus.game.guide;

import kidridicarus.agency.guide.BasicAdvice;
import kidridicarus.agency.guide.SuperAdvice;

public class GameAdvice extends BasicAdvice {
	public boolean jump;
	public boolean runShoot;

	public GameAdvice() {
		clear();
	}

	public GameAdvice(boolean moveRight, boolean moveUp, boolean moveLeft, boolean moveDown, boolean jump,
			boolean runShoot) {
		this.moveRight = moveRight;
		this.moveUp = moveUp;
		this.moveLeft = moveLeft;
		this.moveDown = moveDown;
		this.jump = jump;
		this.runShoot = runShoot;
	}

	@Override
	public void clear() {
		super.clear();
		jump = false;
		runShoot = false;
	}

	public GameAdvice cpy() {
		return new GameAdvice(moveRight, moveUp, moveLeft, moveDown, jump, runShoot);
	}

	public void fromSuperAdvice(SuperAdvice superAdvice) {
		moveRight = superAdvice.moveRight;
		moveUp = superAdvice.moveUp;
		moveLeft = superAdvice.moveLeft;
		moveDown = superAdvice.moveDown;
		jump = superAdvice.action0;
		runShoot = superAdvice.action1;
	}
}
