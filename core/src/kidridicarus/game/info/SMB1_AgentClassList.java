package kidridicarus.game.info;

import kidridicarus.agency.tool.AgentClassList;
import kidridicarus.game.SMB1.agent.NPC.goomba.Goomba;
import kidridicarus.game.SMB1.agent.NPC.turtle.Turtle;
import kidridicarus.game.SMB1.agent.item.fireflower.FireFlower;
import kidridicarus.game.SMB1.agent.item.mushroom.MagicMushroom;
import kidridicarus.game.SMB1.agent.item.mushroom.Up1Mushroom;
import kidridicarus.game.SMB1.agent.item.powerstar.PowerStar;
import kidridicarus.game.SMB1.agent.item.staticcoin.StaticCoin;
import kidridicarus.game.SMB1.agent.other.brickpiece.BrickPiece;
import kidridicarus.game.SMB1.agent.other.bumptile.BumpTile;
import kidridicarus.game.SMB1.agent.other.castleflag.CastleFlag;
import kidridicarus.game.SMB1.agent.other.flagpole.Flagpole;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.SMB1.agent.other.spincoin.SpinCoin;
import kidridicarus.game.SMB1.agent.player.mario.Mario;
import kidridicarus.game.SMB1.agent.player.mariofireball.MarioFireball;

/*
 * Title: Super Mario Bros. Info
 * Desc: Info specific to Super Mario Bros.
 */
public class SMB1_AgentClassList {
	public static final AgentClassList SMB_AGENT_CLASSLIST = new AgentClassList( 
			SMB1_KV.AgentClassAlias.VAL_BRICKPIECE, BrickPiece.class,
			SMB1_KV.AgentClassAlias.VAL_BUMPTILE, BumpTile.class,
			SMB1_KV.AgentClassAlias.VAL_CASTLEFLAG, CastleFlag.class,
			SMB1_KV.AgentClassAlias.VAL_COIN, StaticCoin.class,
			SMB1_KV.AgentClassAlias.VAL_FIREFLOWER, FireFlower.class,
			SMB1_KV.AgentClassAlias.VAL_FLAGPOLE, Flagpole.class,
			SMB1_KV.AgentClassAlias.VAL_FLOATINGPOINTS, FloatingPoints.class,
			SMB1_KV.AgentClassAlias.VAL_GOOMBA, Goomba.class,
			SMB1_KV.AgentClassAlias.VAL_MARIO, Mario.class,
			SMB1_KV.AgentClassAlias.VAL_MARIOFIREBALL, MarioFireball.class,
			SMB1_KV.AgentClassAlias.VAL_MAGIC_MUSHROOM, MagicMushroom.class,
			SMB1_KV.AgentClassAlias.VAL_UP1_MUSHROOM, Up1Mushroom.class,
			SMB1_KV.AgentClassAlias.VAL_POWERSTAR, PowerStar.class,
			SMB1_KV.AgentClassAlias.VAL_SPINCOIN, SpinCoin.class,
			SMB1_KV.AgentClassAlias.VAL_TURTLE, Turtle.class);
}
