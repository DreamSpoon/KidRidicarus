package kidridicarus.common.tool;

import kidridicarus.common.info.CommonKV;

public enum Direction8  {
	RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, NONE;

	public static Direction8 fromString(String str) {
		if(str == null)
			return Direction8.NONE;
		else if(str.equals(CommonKV.VAL_RIGHT))
			return Direction8.RIGHT;
		else if(str.equals(CommonKV.VAL_UP_RIGHT))
			return Direction8.UP_RIGHT;
		else if(str.equals(CommonKV.VAL_UP))
			return Direction8.UP;
		else if(str.equals(CommonKV.VAL_UP_LEFT))
			return Direction8.UP_LEFT;
		else if(str.equals(CommonKV.VAL_LEFT))
			return Direction8.LEFT;
		else if(str.equals(CommonKV.VAL_DOWN_LEFT))
			return Direction8.DOWN_LEFT;
		else if(str.equals(CommonKV.VAL_DOWN))
			return Direction8.DOWN;
		else if(str.equals(CommonKV.VAL_DOWN_RIGHT))
			return Direction8.DOWN_RIGHT;
		else
			return Direction8.NONE;
	}
}
