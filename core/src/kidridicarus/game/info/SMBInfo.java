package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.SMB.BrickPiece;
import kidridicarus.game.agent.SMB.BumpTile;
import kidridicarus.game.agent.SMB.CastleFlag;
import kidridicarus.game.agent.SMB.Flagpole;
import kidridicarus.game.agent.SMB.FloatingPoints;
import kidridicarus.game.agent.SMB.LevelEndTrigger;
import kidridicarus.game.agent.SMB.SpinCoin;
import kidridicarus.game.agent.SMB.NPC.Goomba;
import kidridicarus.game.agent.SMB.NPC.Turtle;
import kidridicarus.game.agent.SMB.item.FireFlower;
import kidridicarus.game.agent.SMB.item.Mush1UP;
import kidridicarus.game.agent.SMB.item.PowerMushroom;
import kidridicarus.game.agent.SMB.item.PowerStar;
import kidridicarus.game.agent.SMB.item.StaticCoin;
import kidridicarus.game.agent.SMB.player.Mario;
import kidridicarus.game.agent.SMB.player.MarioFireball;

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
		if(strAmt.equals(GameKV.SMB.VAL_POINTS0))
			return PointAmount.ZERO;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS100))
			return PointAmount.P100;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS200))
			return PointAmount.P200;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS400))
			return PointAmount.P400;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS500))
			return PointAmount.P500;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS800))
			return PointAmount.P800;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS1000))
			return PointAmount.P1000;
		else if(strAmt.equals(GameKV.SMB.VAL_POINTS1UP))
			return PointAmount.P1UP;
		return PointAmount.ZERO;
	}

	public static String pointAmountToStr(PointAmount amt) {
		switch(amt) {
			default:
			case ZERO:
				return GameKV.SMB.VAL_POINTS0;
			case P100:
				return GameKV.SMB.VAL_POINTS100;
			case P200:
				return GameKV.SMB.VAL_POINTS200;
			case P400:
				return GameKV.SMB.VAL_POINTS400;
			case P500:
				return GameKV.SMB.VAL_POINTS500;
			case P800:
				return GameKV.SMB.VAL_POINTS800;
			case P1000:
				return GameKV.SMB.VAL_POINTS1000;
			case P2000:
				return GameKV.SMB.VAL_POINTS2000;
			case P4000:
				return GameKV.SMB.VAL_POINTS4000;
			case P5000:
				return GameKV.SMB.VAL_POINTS5000;
			case P8000:
				return GameKV.SMB.VAL_POINTS8000;
			case P1UP:
				return GameKV.SMB.VAL_POINTS1UP;
		}
	}

	public static final AgentClassList SMB_AGENT_CLASSLIST = new AgentClassList( 
			GameKV.Level.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			GameKV.SMB.VAL_BRICKPIECE, BrickPiece.class,
			GameKV.SMB.VAL_BUMPTILE, BumpTile.class,
			GameKV.SMB.VAL_CASTLEFLAG, CastleFlag.class,
			GameKV.SMB.VAL_COIN, StaticCoin.class,
			GameKV.SMB.VAL_FIREFLOWER, FireFlower.class,
			GameKV.SMB.VAL_FLAGPOLE, Flagpole.class,
			GameKV.SMB.VAL_FLOATINGPOINTS, FloatingPoints.class,
			GameKV.SMB.VAL_GOOMBA, Goomba.class,
			GameKV.SMB.VAL_MARIO, Mario.class,
			GameKV.SMB.VAL_MARIOFIREBALL, MarioFireball.class,
			GameKV.SMB.VAL_MUSHROOM, PowerMushroom.class,
			GameKV.SMB.VAL_MUSH1UP, Mush1UP.class,
			GameKV.SMB.VAL_POWERSTAR, PowerStar.class,
			GameKV.SMB.VAL_SPINCOIN, SpinCoin.class,
			GameKV.SMB.VAL_TURTLE, Turtle.class);
}
