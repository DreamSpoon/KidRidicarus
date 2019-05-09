package kidridicarus.game.SMB1.agent.other.bumptile;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;
import kidridicarus.game.SMB1.agent.TileBumpTakeAgent.TileBumpStrength;
import kidridicarus.game.SMB1.agent.item.fireflower.FireFlower;
import kidridicarus.game.SMB1.agent.item.mushroom.MagicMushroom;
import kidridicarus.game.SMB1.agent.item.mushroom.Up1Mushroom;
import kidridicarus.game.SMB1.agent.item.powerstar.PowerStar;
import kidridicarus.game.SMB1.agent.other.brickpiece.BrickPiece;
import kidridicarus.game.SMB1.agent.other.floatingpoints.FloatingPoints;
import kidridicarus.game.SMB1.agent.other.spincoin.SpinCoin;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.info.SMB1_Pow;

public class BumpTileBrain {
	private static final float BOUNCE_TIME = 0.175f;
	private static final float BOUNCE_HEIGHT_FRAC = 0.225f;	// bounce up about 1/5 of tile height

	private static final float BREAKRIGHT_VEL1_X = 1f;
	private static final float BREAKRIGHT_VEL1_Y = 3f;
	private static final float BREAKRIGHT_VEL2_X = 1f;
	private static final float BREAKRIGHT_VEL2_Y = 4f;

	private static final float COIN_BUMP_RESET_TIME = 0.23f;
	private static final float MAX_COIN_BUMP_TIME = 3f;

	// only blocks with items can reach the empty state
	private enum MoveState { PRESOLID, PREBUMP, MIDBUMP, EMPTY }
	private enum BlockItem { NONE, COIN, COIN10, MAGIC_MUSHROOM, UP1_MUSHROOM, STAR }

	private BumpTile parent;
	private BumpTileBody body;
	private MoveState moveState;
	private float moveStateTimer;
	private SolidTiledMapAgent solidTileMap;
	private Agent bumpingAgent;
	private TileBumpStrength bumpStrength;
	private boolean isSecret;
	private boolean isEmpty;
	private BlockItem blockItem;
	private int coin10Coins;
	private float coin10BumpResetTimer;
	private float coin10EndTimer;

	public BumpTileBrain(BumpTile parent, BumpTileBody body, boolean isSecret, String spawnItem) {
		this.parent = parent;
		this.body = body;
		this.isSecret = isSecret;
		bumpingAgent = null;
		bumpStrength = TileBumpStrength.NONE;
		moveState = MoveState.PRESOLID;
		moveStateTimer = 0f;
		coin10Coins = 10;
		coin10BumpResetTimer = 0;
		coin10EndTimer = 0;
		solidTileMap = null;
		isEmpty = false;
		blockItem = BlockItem.NONE;
		if(spawnItem.equals(SMB1_KV.AgentClassAlias.VAL_COIN))
			blockItem = BlockItem.COIN;
		else if(spawnItem.equals(SMB1_KV.VAL_COIN10)) {
			blockItem = BlockItem.COIN10;
			coin10Coins = 10;
			coin10BumpResetTimer = 0f;
			coin10EndTimer = 0f;
		}
		else if(spawnItem.equals(SMB1_KV.AgentClassAlias.VAL_MAGIC_MUSHROOM))
			blockItem = BlockItem.MAGIC_MUSHROOM;
		else if(spawnItem.equals(SMB1_KV.AgentClassAlias.VAL_POWERSTAR))
			blockItem = BlockItem.STAR;
		else if(spawnItem.equals(SMB1_KV.AgentClassAlias.VAL_UP1_MUSHROOM))
			blockItem = BlockItem.UP1_MUSHROOM;
	}

	public BumpTileSpriteFrameInput processFrame(FrameTime frameTime) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case PRESOLID:
				// Check for first contact with solid tile map every frame until found,
				// make the tile solid in the tile physics layer if it is not a secret block.
				solidTileMap = body.getSpine().getSolidTileMap();
				if(solidTileMap != null && !isSecret)
					solidTileMap.setTileSolidStateAtPos(body.getPosition(), true);
				break;
			case PREBUMP:
			case EMPTY:
				// last frame was midbump?
				if(moveState == MoveState.MIDBUMP)
					onBounceEnd();
				break;
			case MIDBUMP:
				// first frame of midbump?
				if(moveState != nextMoveState)
					onBounceStart();
				break;
		}

		if(blockItem == BlockItem.COIN10) {
			if(coin10BumpResetTimer >= frameTime.timeDelta)
				coin10BumpResetTimer -= frameTime.timeDelta;
			else
				coin10BumpResetTimer = 0f;

			if(coin10EndTimer > 0f)
				coin10EndTimer -= frameTime.timeDelta;
		}

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+frameTime.timeDelta : 0f;
		moveState = nextMoveState;

		return new BumpTileSpriteFrameInput(
				body.getPosition().cpy().add(0f, getCurrentBounceHeight()), frameTime, isEmpty);
	}

	private MoveState getNextMoveState() {
		if(moveState == MoveState.EMPTY)
			return MoveState.EMPTY;
		else if(moveState == MoveState.MIDBUMP) {
			if(moveStateTimer <= BOUNCE_TIME)
				return MoveState.MIDBUMP;
			else if(isEmpty)
				return MoveState.EMPTY;
			else
				return MoveState.PREBUMP;
		}
		else if(bumpStrength != TileBumpStrength.NONE)
			return MoveState.MIDBUMP;
		else if(solidTileMap != null)
			return MoveState.PREBUMP;
		return MoveState.PRESOLID;
	}

	private void onBounceStart() {
		bopTopGoombas();

		if(blockItem == BlockItem.NONE && bumpStrength == TileBumpStrength.HARD)
			startBreakTile();
		else {
			// if the tile was a secret block then it was not solid, so make it solid
			if(isSecret)
				solidTileMap.setTileSolidStateAtPos(body.getPosition(), true);

			switch(blockItem) {
				case COIN:
					isEmpty = true;
					startSpinningCoin();
					break;
				case COIN10:
					if(coin10BumpResetTimer > 0f)
						break;

					// first time this tile has been bumped?
					if(coin10EndTimer == 0f)
						coin10EndTimer = MAX_COIN_BUMP_TIME;
					// if the timer is less than zero then disable the block
					else if(coin10EndTimer < 0f)
						isEmpty = true;

					coin10Coins--;
					// if last coin bumped then disable the block
					if(coin10Coins < 1)
						isEmpty = true;
					else
						coin10BumpResetTimer = COIN_BUMP_RESET_TIME;

					startSpinningCoin();
					break;
				case NONE:
					break;
				default:
					isEmpty = true;
					break;
			}
		}
	}

	private void bopTopGoombas() {
		// use QueryAABB to build agentsOnMe list
		final ArrayList<Agent> agentsOnMe;

		agentsOnMe = new ArrayList<Agent>();
		// check for agents in an area slightly thinner than the tile, and only as tall as the tile bounces
		// (shrink the box a bit so we don't get enemies on adjacent tiles -
		// TODO: find a more accurate QueryAABB method)
		parent.getAgency().getWorld().QueryAABB(
				new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {
						if(fixture.getUserData() instanceof AgentBodyFilter) {
							Agent a = AgentBodyFilter.getAgentFromFilter((AgentBodyFilter) fixture.getUserData());
							if(a != null)
								agentsOnMe.add(a);
						}
						return true;
					}
				}, body.getPosition().x - body.getBounds().width/2f*0.25f, body.getPosition().y + body.getBounds().height/2f,
				body.getPosition().x + body.getBounds().width/2f*0.25f,
				body.getPosition().y + body.getBounds().height/2f + body.getBounds().height*BOUNCE_HEIGHT_FRAC);

		// bop any bumpable agents that are standing on the tile
		Iterator<Agent> iter = agentsOnMe.iterator();
		while(iter.hasNext()) {
			Agent agent = iter.next();
			if(agent instanceof BumpTakeAgent)
				((BumpTakeAgent) agent).onTakeBump(bumpingAgent);
		}
	}

	private void onBounceEnd() {
		Vector2 pos = body.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y));
		switch(blockItem) {
			case UP1_MUSHROOM:
				parent.getAgency().createAgent(Up1Mushroom.makeAP(pos));
				parent.getAgency().getEar().playSound(SMB1_Audio.Sound.POWERUP_SPAWN);
				break;
			case MAGIC_MUSHROOM:
				// hard bump gives a fireflower, soft bump gives mushroom
				if(bumpStrength == TileBumpStrength.HARD)
					parent.getAgency().createAgent(FireFlower.makeAP(pos));
				else
					parent.getAgency().createAgent(MagicMushroom.makeAP(pos));
				parent.getAgency().getEar().playSound(SMB1_Audio.Sound.POWERUP_SPAWN);
				break;
			case STAR:
				parent.getAgency().createAgent(PowerStar.makeAP(pos));
				parent.getAgency().getEar().playSound(SMB1_Audio.Sound.POWERUP_SPAWN);
				break;
			default:
				break;
		}

		// clear the bump flag, to prevent rebump
		bumpStrength = TileBumpStrength.NONE;
		bumpingAgent = null;
	}

	private void startBreakTile() {
		solidTileMap.setTileSolidStateAtPos(body.getPosition(), false);

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		float right = body.getBounds().width / 4f;
		float up = body.getBounds().height / 4f;
		// replace the tile with 4 brick pieces shooting upward and outward
		parent.getAgency().createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(right, up),
				new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		parent.getAgency().createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(right, -up),
				new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		parent.getAgency().createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(-right, up),
				new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		parent.getAgency().createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(-right, -up),
				new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));

		parent.getAgency().getEar().playSound(SMB1_Audio.Sound.BREAK);
		parent.getAgency().removeAgent(parent);

		Powerup.tryPushPowerup(bumpingAgent, new SMB1_Pow.PointsPow(100));
	}

	private void startSpinningCoin() {
		parent.getAgency().createAgent(FloatingPoints.makeAP(200, false, body.getPosition(), bumpingAgent));
		// spawn a coin one tile's height above the current tile position
		parent.getAgency().createAgent(SpinCoin.makeAP(body.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y))));
		parent.getAgency().getEar().playSound(SMB1_Audio.Sound.COIN);
		// push coin powerup to powerup take Agent if Agent exists
		Powerup.tryPushPowerup(bumpingAgent, new SMB1_Pow.CoinPow());
	}

	private float getCurrentBounceHeight() {
		if(moveState != MoveState.MIDBUMP)
			return 0f;
		// linear bounce up to max height at halftime, then return down to original height at endtime
		if(moveStateTimer <= BOUNCE_TIME/2)
			return moveStateTimer / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * body.getBounds().height;
		else
			return (BOUNCE_TIME-moveStateTimer) / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * body.getBounds().height;
	}

	public boolean onTakeTileBump(Agent agent, TileBumpStrength strength) {
		// If tile did contain an item, but the item was used, then no bump allowed.
		// If this tile is not in pre-bump state then no bump allowed.
		// If bumped already or if new bump strength is NONE then no bump allowed.
		if(isEmpty || moveState != MoveState.PREBUMP || bumpStrength != TileBumpStrength.NONE ||
				strength == TileBumpStrength.NONE)
			return false;
		// keep refs to perpetrator and strength of hit
		bumpingAgent = agent;
		bumpStrength = strength;
		return true;
	}
}
