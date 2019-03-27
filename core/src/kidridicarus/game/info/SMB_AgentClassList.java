package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.SMB.NPC.goomba.Goomba;
import kidridicarus.game.agent.SMB.NPC.turtle.Turtle;
import kidridicarus.game.agent.SMB.item.fireflower.FireFlower;
import kidridicarus.game.agent.SMB.item.mushroom.Mush1UP;
import kidridicarus.game.agent.SMB.item.mushroom.PowerMushroom;
import kidridicarus.game.agent.SMB.item.powerstar.PowerStar;
import kidridicarus.game.agent.SMB.item.staticcoin.StaticCoin;
import kidridicarus.game.agent.SMB.other.brickpiece.BrickPiece;
import kidridicarus.game.agent.SMB.other.bumptile.BumpTile;
import kidridicarus.game.agent.SMB.other.castleflag.CastleFlag;
import kidridicarus.game.agent.SMB.other.flagpole.Flagpole;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.agent.SMB.other.spincoin.SpinCoin;
import kidridicarus.game.agent.SMB.player.mario.Mario;
import kidridicarus.game.agent.SMB.player.mariofireball.MarioFireball;

/*
 * Title: Super Mario Bros. Info
 * Desc: Info specific to Super Mario Bros.
 */
public class SMB_AgentClassList {
	public static final AgentClassList SMB_AGENT_CLASSLIST = new AgentClassList( 
			SMB_KV.AgentClassAlias.VAL_BRICKPIECE, BrickPiece.class,
			SMB_KV.AgentClassAlias.VAL_BUMPTILE, BumpTile.class,
			SMB_KV.AgentClassAlias.VAL_CASTLEFLAG, CastleFlag.class,
			SMB_KV.AgentClassAlias.VAL_COIN, StaticCoin.class,
			SMB_KV.AgentClassAlias.VAL_FIREFLOWER, FireFlower.class,
			SMB_KV.AgentClassAlias.VAL_FLAGPOLE, Flagpole.class,
			SMB_KV.AgentClassAlias.VAL_FLOATINGPOINTS, FloatingPoints.class,
			SMB_KV.AgentClassAlias.VAL_GOOMBA, Goomba.class,
			SMB_KV.AgentClassAlias.VAL_MARIO, Mario.class,
			SMB_KV.AgentClassAlias.VAL_MARIOFIREBALL, MarioFireball.class,
			SMB_KV.AgentClassAlias.VAL_MUSHROOM, PowerMushroom.class,
			SMB_KV.AgentClassAlias.VAL_MUSH1UP, Mush1UP.class,
			SMB_KV.AgentClassAlias.VAL_POWERSTAR, PowerStar.class,
			SMB_KV.AgentClassAlias.VAL_SPINCOIN, SpinCoin.class,
			SMB_KV.AgentClassAlias.VAL_TURTLE, Turtle.class);
}
