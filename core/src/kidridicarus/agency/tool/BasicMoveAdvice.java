package kidridicarus.agency.tool;

/*
 * Move, as in chess move. The advice is intended to come from the player to be used by an agent.
 */
public class BasicMoveAdvice {
	public boolean moveRight;
	public boolean moveUp;
	public boolean moveLeft;
	public boolean moveDown;

	public BasicMoveAdvice() {
		clear();
	}

	public void clear() {
		moveRight = false;
		moveUp = false;
		moveLeft = false;
		moveDown = false;
	}
}
