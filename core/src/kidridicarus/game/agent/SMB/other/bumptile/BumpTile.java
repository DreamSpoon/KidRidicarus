package kidridicarus.game.agent.SMB.other.bumptile;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.collision.CollisionTiledMapAgent;
import kidridicarus.game.agent.SMB.BumpTakeAgent;
import kidridicarus.game.agent.SMB.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB.other.brickpiece.BrickPiece;
import kidridicarus.game.agent.SMB.other.floatingpoints.FloatingPoints;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo.PointAmount;

public class BumpTile extends Agent implements TileBumpTakeAgent, DisposableAgent {
	public enum TileBumpStrength { NONE, SOFT, HARD }

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
	private enum BlockItem { NONE, COIN, COIN10, MUSHROOM, STAR, MUSH1UP }

	private BumpTileBody body;
	private BumpTileSprite sprite;

	private boolean isQblock;
	private boolean isItemAvailable;
	private BlockItem blockItem;
	private int coin10Coins;
	private float coin10BumpResetTimer;
	private float coin10EndTimer;

	private TileBumpStrength bumpStrength;
	private Agent bumpingAgent;
	private CollisionTiledMapAgent collisionMap; 
	private MoveState moveState;
	private float moveStateTimer;

	public BumpTile(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		bumpingAgent = null;
		bumpStrength = TileBumpStrength.NONE;
		moveState = MoveState.PRESOLID;
		moveStateTimer = 0f;

		coin10Coins = 10;
		coin10BumpResetTimer = 0;
		coin10EndTimer = 0;

		isQblock = properties.containsKey(GameKV.SMB.KEY_QBLOCK);

		blockItem = BlockItem.NONE;
		String spawnItem = properties.get(GameKV.SMB.KEY_SPAWNITEM, "", String.class);
		if(spawnItem.equals(GameKV.SMB.AgentClassAlias.VAL_COIN))
			blockItem = BlockItem.COIN;
		else if(spawnItem.equals(GameKV.SMB.VAL_COIN10)) {
			blockItem = BlockItem.COIN10;
			coin10Coins = 10;
			coin10BumpResetTimer = 0f;
			coin10EndTimer = 0f;
		}
		else if(spawnItem.equals(GameKV.SMB.AgentClassAlias.VAL_MUSHROOM))
			blockItem = BlockItem.MUSHROOM;
		else if(spawnItem.equals(GameKV.SMB.AgentClassAlias.VAL_POWERSTAR))
			blockItem = BlockItem.STAR;
		else if(spawnItem.equals(GameKV.SMB.AgentClassAlias.VAL_MUSH1UP))
			blockItem = BlockItem.MUSH1UP;
		isItemAvailable = blockItem != BlockItem.NONE;

		collisionMap = null;

		body = new BumpTileBody(agency.getWorld(), this, Agent.getStartBounds(properties));
		sprite = new BumpTileSprite(agency.getAtlas(), Agent.getStartTexRegion(properties));

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doRegularUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doRegularUpdate(float delta) {
		processMove(delta);
		processSprite(delta);
	}

	private void processMove(float delta) {
		MoveState nextMoveState = getNextMoveState();
		switch(nextMoveState) {
			case PRESOLID:
				// check for first contact with collision map every frame until found
				collisionMap = body.getSpine().getCollisionMap();
				if(collisionMap != null) {
					// make the tile solid in the tile physics layer if it is not a secret block 
					if(!properties.containsKV(GameKV.SMB.KEY_SECRETBLOCK, CommonKV.VAL_TRUE))
						collisionMap.setTileSolidStateAtPos(body.getPosition(), true);
				}
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
			if(coin10BumpResetTimer >= delta)
				coin10BumpResetTimer -= delta;
			else
				coin10BumpResetTimer = 0f;

			if(coin10EndTimer > 0f)
				coin10EndTimer -= delta;
		}

		moveStateTimer = nextMoveState == moveState ? moveStateTimer+delta : 0f;
		moveState = nextMoveState;
	}

	private boolean isEmptyItemBlock() {
		if(blockItem == BlockItem.NONE)
			return false;
		return !isItemAvailable;
	}

	private MoveState getNextMoveState() {
		if(moveState == MoveState.EMPTY)
			return MoveState.EMPTY;
		else if(moveState == MoveState.MIDBUMP) {
			if(moveStateTimer <= BOUNCE_TIME)
				return MoveState.MIDBUMP;
			else if(blockItem != BlockItem.NONE && !isItemAvailable)
				return MoveState.EMPTY;
			else
				return MoveState.PREBUMP;
		}
		else if(bumpStrength != TileBumpStrength.NONE)
			return MoveState.MIDBUMP;
		else if(collisionMap != null)
			return MoveState.PREBUMP;
		return MoveState.PRESOLID;
	}

	private void onBounceStart() {
		bopTopGoombas();

		if(blockItem == BlockItem.NONE && bumpStrength == TileBumpStrength.HARD)
			startBreakTile();
		else {
			// if the tile was a secret block then it was not solid, so make it solid 
			if(properties.get(GameKV.SMB.KEY_SECRETBLOCK, "", String.class).equals(CommonKV.VAL_TRUE))
				collisionMap.setTileSolidStateAtPos(body.getPosition(), true);

			switch(blockItem) {
				case COIN:
					isItemAvailable = false;
//					if(bumpingAgent instanceof Mario)
//						((Mario) bumpingAgent).giveCoin();
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
						isItemAvailable = false;

					coin10Coins--;
					// if last coin bumped then disable the block 
					if(coin10Coins < 1)
						isItemAvailable = false;
					else
						coin10BumpResetTimer = COIN_BUMP_RESET_TIME;

//					if(bumpingAgent instanceof Mario)
//						((Mario) bumpingAgent).giveCoin();
					startSpinningCoin();
					break;
				default:
					isItemAvailable = false;
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
		agency.getWorld().QueryAABB(
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
			case MUSH1UP:
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_SPAWN);
				agency.createAgent(Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_MUSH1UP, pos));
				break;
			case MUSHROOM:
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_SPAWN);
				// big mario pops a fireflower?
				if(bumpStrength == TileBumpStrength.HARD)
					agency.createAgent(Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_FIREFLOWER, pos));
				else
					agency.createAgent(Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_MUSHROOM, pos));
				break;
			case STAR:
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_SPAWN);
				agency.createAgent(Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_POWERSTAR, pos));
				break;
			default:
				break;
		}

		// clear the bump flag, to prevent rebump
		bumpStrength = TileBumpStrength.NONE;
		bumpingAgent = null;
	}

	private void startBreakTile() {
		collisionMap.setTileSolidStateAtPos(body.getPosition(), false);

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		float right = body.getBounds().width / 4f;
		float up = body.getBounds().height / 4f;
		// replace the tile with 4 brick pieces shooting upward and outward
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(right, up),
				new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(right, -up),
				new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(-right, up),
				new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(-right, -up),
				new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));

		agency.playSound(AudioInfo.Sound.SMB.BREAK);
//		((Mario) bumpingAgent).givePoints(PointAmount.P200, false);

		agency.disposeAgent(this);
	}

	private void startSpinningCoin() {
		agency.playSound(AudioInfo.Sound.SMB.COIN);
		agency.createAgent(FloatingPoints.makeAP(PointAmount.P200, false, body.getPosition(),
				UInfo.P2M(UInfo.TILEPIX_Y * 2f), bumpingAgent));

		// spawn a coin one tile's height above the current tile position
		agency.createAgent(Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_SPINCOIN,
				body.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y))));

		// push coin powerup to powerup take agent if we have one
		if(bumpingAgent instanceof PowerupTakeAgent)
			((PowerupTakeAgent) bumpingAgent).onTakePowerup(PowType.COIN);
	}

	private void processSprite(float delta) {
		// TODO: this offsetY code should go in the sprite, not here
		float offsetY = 0f;
		if(moveState == MoveState.MIDBUMP) {
			// linear bounce up to max height at halftime, then return down to original height at endtime
			// time to go up?
			if(moveStateTimer <= BOUNCE_TIME/2)
				offsetY = moveStateTimer / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * body.getBounds().height;
			else {	// time to go down
				offsetY = (BOUNCE_TIME-moveStateTimer) / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC *
						body.getBounds().height;
			}
		}
		sprite.update(delta, agency.getGlobalTimer(), body.getPosition().add(0f, offsetY), isQblock,
				isEmptyItemBlock());
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(sprite);
	}

	/*
	 * Returns false if tile bump not taken (e.g. because tile is already bumped, etc.).
	 * Otherwise returns true.
	 */
	@Override
	public boolean onTakeTileBump(Agent agent, TileBumpStrength strength) {
		// if tile usually contains an item, but the item has been used, then no bump allowed
		if(isEmptyItemBlock())
			return false;
		// bump allowed only if this tile is in pre-bump state
		if(moveState != MoveState.PREBUMP)
			return false;
		// exit if this tile is bumped already or if new bump strength is NONE
		if(bumpStrength != TileBumpStrength.NONE || strength == TileBumpStrength.NONE)
			return false;
		// keep refs to perpetrator and strength of hit
		bumpingAgent = agent;
		bumpStrength = strength;
		return true;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
