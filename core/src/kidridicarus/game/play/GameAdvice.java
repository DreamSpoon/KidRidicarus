package kidridicarus.game.play;

import kidridicarus.agency.tool.BasicAdvice;
import kidridicarus.agency.tool.Direction4;
import kidridicarus.agency.tool.SuperAdvice;

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

	/*
	 * Returns a Direction4 object based on the current state of the move advice (right, up, left, down).
	 * Returns null if move advice is all false (i.e. no move right, no move up, etc.) or if move advice is
	 * ambiguous (i.e. if all 4 move directions are true concurrently). Intended to be robust; returns the
	 * correct direction when one axis has ambiguous move advice, and the other axis has discernible advice. 
	 */
	public Direction4 getMoveDir4() {
		// XOR the right/left move to disallow move both right and left concurrently
		boolean h = moveRight ^ moveLeft;
		// XOR the up/down move to disallow move both up and down concurrently
		boolean v = moveUp ^ moveDown;

		// XOR the horizontal/vertical move to disallow move both horizontally and vertically concurrently
		// return null if no direction available based on this advice
		if(h ^ v == false)
			return null;
		// is the move horizontal?
		else if(h) {
			if(moveRight)
				return Direction4.RIGHT;
			else
				return Direction4.LEFT;
		}
		// vertical move
		else {
			if(moveUp)
				return Direction4.UP;
			else
				return Direction4.DOWN;
		}
	}
}
