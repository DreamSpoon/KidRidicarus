package com.ridicarus.kid;

public class InfoSMB {
	public enum PowerupType { NONE, MUSHROOM, FIREFLOWER, POWERSTAR };

	// https://www.mariowiki.com/Point
	//   Super Mario Bros. 1
	//     100 - 200 - 400 - 500 - 800 - 1000 - 2000 - 4000 - 5000 - 8000 - 1UP 
	public enum PointsAmount {
		// Points begin at zero, and end at infinity.
		// Zero is used to mark uninitialized variables.
		// Infinity is used to mark overflow (incrementing past known values).
		ZERO(0), P100(100), P200(200), P400(400), P500(500), P800(800), P1000(1000), P2000(2000), P4000(4000),
			P5000(5000), P8000(8000), INFINITY(0);
		private int amt;
		PointsAmount(int amt) { this.amt = amt; }
		public int getAmt() { return amt; }
		public PointsAmount increment() {
			if(ordinal()+1 < values().length)
				return PointsAmount.values()[ordinal()+1];
			else
				return PointsAmount.INFINITY;
		}
	};

	public static final float MARIO_DEAD_TIME = 3f;
	public static final float MARIO_LEVELEND_TIME = 5f;
}
