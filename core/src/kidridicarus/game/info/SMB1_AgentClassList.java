package kidridicarus.game.info;

import kidridicarus.agency.AgentClassList;
import kidridicarus.game.agent.SMB1.NPC.goomba.Goomba;
import kidridicarus.game.agent.SMB1.NPC.turtle.Turtle;
import kidridicarus.game.agent.SMB1.item.fireflower.FireFlower;
import kidridicarus.game.agent.SMB1.item.mushroom.Up1Mushroom;
import kidridicarus.game.agent.SMB1.item.mushroom.MagicMushroom;
import kidridicarus.game.agent.SMB1.item.powerstar.PowerStar;
import kidridicarus.game.agent.SMB1.item.staticcoin.StaticCoin;
import kidridicarus.game.agent.SMB1.other.brickpiece.BrickPiece;
import kidridicarus.game.agent.SMB1.other.bumptile.BumpTile;
import kidridicarus.game.agent.SMB1.other.castleflag.CastleFlag;
import kidridicarus.game.agent.SMB1.other.flagpole.Flagpole;
import kidridicarus.game.agent.SMB1.other.floatingpoints.FloatingPoints;
import kidridicarus.game.agent.SMB1.other.spincoin.SpinCoin;
import kidridicarus.game.agent.SMB1.player.mario.Mario;
import kidridicarus.game.agent.SMB1.player.mariofireball.MarioFireball;

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
