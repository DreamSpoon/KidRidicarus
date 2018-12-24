package kidridicarus.guide;

public class Advice {
	public boolean moveRight;
	public boolean moveUp;
	public boolean moveLeft;
	public boolean moveDown;
	public boolean run;
	public boolean jump;

	public Advice() {
		this(false, false, false, false, false, false);
	}

	public Advice(boolean moveRight, boolean moveUp, boolean moveLeft, boolean moveDown, boolean run, boolean jump) {
		this.moveRight = moveRight;
		this.moveUp = moveUp;
		this.moveLeft = moveLeft;
		this.moveDown = moveDown;
		this.run = run;
		this.jump = jump;
	}

	public Advice cpy() {
		return new Advice(moveRight, moveUp, moveLeft, moveDown, run, jump);
	}

	public void clear() {
		moveRight = moveUp = moveLeft = moveRight = run = jump = false; 
	}
}
