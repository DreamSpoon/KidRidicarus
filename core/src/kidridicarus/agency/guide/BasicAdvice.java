package kidridicarus.agency.guide;

public class BasicAdvice {
	public boolean moveRight;
	public boolean moveUp;
	public boolean moveLeft;
	public boolean moveDown;

	public BasicAdvice() {
		clear();
	}

	public void clear() {
		moveRight = false;
		moveUp = false;
		moveLeft = false;
		moveDown = false;
	}
}
