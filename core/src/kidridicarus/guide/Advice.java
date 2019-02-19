package kidridicarus.guide;

public class Advice {
	public boolean moveRight;
	public boolean moveUp;
	public boolean moveLeft;
	public boolean moveDown;
	public boolean run;
	public boolean jump;
	public boolean shoot;

	public Advice() {
		this(false, false, false, false, false, false, false);
	}

	public Advice(boolean moveRight, boolean moveUp, boolean moveLeft, boolean moveDown, boolean run, boolean jump,
			boolean shoot) {
		this.moveRight = moveRight;
		this.moveUp = moveUp;
		this.moveLeft = moveLeft;
		this.moveDown = moveDown;
		this.run = run;
		this.jump = jump;
		this.shoot = shoot;
	}

	public Advice cpy() {
		return new Advice(moveRight, moveUp, moveLeft, moveDown, run, jump, shoot);
	}

	public void clear() {
		moveRight = moveUp = moveLeft = run = jump = shoot = false; 
	}
}
