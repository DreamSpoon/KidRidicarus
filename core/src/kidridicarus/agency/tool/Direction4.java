package kidridicarus.agency.tool;

public enum Direction4 {
	RIGHT, UP, LEFT, DOWN;

	public boolean isHorizontal() {
		return this.equals(RIGHT) || this.equals(LEFT);
	}

	public boolean isVertical() {
		return this.equals(UP) || this.equals(DOWN);
	}

	// rotate 90 degrees counterclockwise
	public Direction4 rotate90() {
		switch(this) {
			case RIGHT:
				return Direction4.UP;
			case UP:
				return Direction4.LEFT;
			case LEFT:
				return Direction4.DOWN;
			default:
				return Direction4.RIGHT;
		}
	}
	// rotate 270 degrees counterclockwise (90 degrees clockwise)
	public Direction4 rotate270() {
		switch(this) {
			case RIGHT:
				return Direction4.DOWN;
			case DOWN:
				return Direction4.LEFT;
			case LEFT:
				return Direction4.UP;
			default:
				return Direction4.RIGHT;
		}
	}
}
