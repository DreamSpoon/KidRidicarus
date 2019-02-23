package kidridicarus.game.info;

/*
 * Title: Super Mario Bros. Info
 * Desc: Info specific to Super Mario Bros.
 */
public class SMBInfo {
	public static final float MARIO_DEAD_TIME = 3f;
	public static final float MARIO_LEVELEND_TIME = 5f;

	// https://www.mariowiki.com/Point
	//   Super Mario Bros. 1
	//     100 - 200 - 400 - 500 - 800 - 1000 - 2000 - 4000 - 5000 - 8000 - 1UP 
	public enum PointAmount {
		// Points begin at zero, and end at infinity.
		// Zero is used to mark uninitialized variables.
		// Infinity is used to mark overflow (incrementing past known values).
		ZERO(0), P100(100), P200(200), P400(400), P500(500), P800(800), P1000(1000), P2000(2000), P4000(4000),
			P5000(5000), P8000(8000), P1UP(0);
		private int amt;
		PointAmount(int amt) { this.amt = amt; }
		public int getIntAmt() { return amt; }
		public PointAmount increment() {
			if(ordinal()+1 < values().length)
				return PointAmount.values()[ordinal()+1];
			// when points overflow occurs, the player gets 1-UP
			else
				return PointAmount.P1UP;
		}
	}

	public static PointAmount strToPointAmount(String strAmt) {
		if(strAmt.equals(KVInfo.SMB.VAL_POINTS0))
			return PointAmount.ZERO;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS100))
			return PointAmount.P100;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS200))
			return PointAmount.P200;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS400))
			return PointAmount.P400;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS500))
			return PointAmount.P500;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS800))
			return PointAmount.P800;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS1000))
			return PointAmount.P1000;
		else if(strAmt.equals(KVInfo.SMB.VAL_POINTS1UP))
			return PointAmount.P1UP;
		return PointAmount.ZERO;
	}

	public static String pointAmountToStr(PointAmount amt) {
		switch(amt) {
			default:
			case ZERO:
				return KVInfo.SMB.VAL_POINTS0;
			case P100:
				return KVInfo.SMB.VAL_POINTS100;
			case P200:
				return KVInfo.SMB.VAL_POINTS200;
			case P400:
				return KVInfo.SMB.VAL_POINTS400;
			case P500:
				return KVInfo.SMB.VAL_POINTS500;
			case P800:
				return KVInfo.SMB.VAL_POINTS800;
			case P1000:
				return KVInfo.SMB.VAL_POINTS1000;
			case P2000:
				return KVInfo.SMB.VAL_POINTS2000;
			case P4000:
				return KVInfo.SMB.VAL_POINTS4000;
			case P5000:
				return KVInfo.SMB.VAL_POINTS5000;
			case P8000:
				return KVInfo.SMB.VAL_POINTS8000;
			case P1UP:
				return KVInfo.SMB.VAL_POINTS1UP;
		}
	}
}
