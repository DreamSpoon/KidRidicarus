package kidridicarus.common.tool;

public class MoveAdvice {
	public boolean moveRight;
	public boolean moveUp;
	public boolean moveLeft;
	public boolean moveDown;
	public boolean action0;
	public boolean action1;

	public MoveAdvice() {
		clear();
	}

	public MoveAdvice(MoveAdvice moveAdvice) {
		set(moveAdvice);
	}

	public void clear() {
		moveRight = false;
		moveUp = false;
		moveLeft = false;
		moveDown = false;
		action0 = false;
		action1 = false;
	}

	public void set(MoveAdvice other) {
		this.moveRight = other.moveRight;
		this.moveUp = other.moveUp;
		this.moveLeft = other.moveLeft;
		this.moveDown = other.moveDown;
		this.action0 = other.action0;
		this.action1 = other.action1;
	}

	public MoveAdvice cpy() {
		return new MoveAdvice(this);
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
