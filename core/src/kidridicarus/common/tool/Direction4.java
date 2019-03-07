package kidridicarus.common.tool;

import kidridicarus.agency.info.AgencyKV;

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

	public static Direction4 fromString(String str) {
		if(str == null)
			return null;
		else if(str.equals(AgencyKV.VAL_RIGHT))
			return Direction4.RIGHT;
		else if(str.equals(AgencyKV.VAL_UP))
			return Direction4.UP;
		else if(str.equals(AgencyKV.VAL_LEFT))
			return Direction4.LEFT;
		else if(str.equals(AgencyKV.VAL_DOWN))
			return Direction4.DOWN;
		else
			return null;
	}
}
