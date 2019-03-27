package kidridicarus.common.tool;

import kidridicarus.common.info.CommonKV;

/*
 * A wrapper for 4 directions:
 *       UP
 * 
 *  LEFT    RIGHT
 *  
 *      DOWN
 *  
 * Note: Can be extended with other enums to cover 4 diagonal directions,
 *   or 8 directions (UP-LEFT, UP, UP-RIGHT, etc.).
 */
public enum Direction4 {
	// NONE enum created to prevent problems with returning nulls when Direction4 is not right, up, left, or down
	RIGHT, UP, LEFT, DOWN, NONE;

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
			case DOWN:
				return Direction4.RIGHT;
			default:
				return Direction4.NONE;
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
			case UP:
				return Direction4.RIGHT;
			default:
				return Direction4.NONE;
		}
	}

	public static Direction4 fromString(String str) {
		if(str == null)
			return Direction4.NONE;
		else if(str.equals(CommonKV.VAL_RIGHT))
			return Direction4.RIGHT;
		else if(str.equals(CommonKV.VAL_UP))
			return Direction4.UP;
		else if(str.equals(CommonKV.VAL_LEFT))
			return Direction4.LEFT;
		else if(str.equals(CommonKV.VAL_DOWN))
			return Direction4.DOWN;
		else
			return Direction4.NONE;
	}

	public boolean isRight() {
		return this == Direction4.RIGHT;
	}
}
