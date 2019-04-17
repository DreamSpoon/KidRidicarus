package kidridicarus.game.agent.SMB1.other.bumptile;

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
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.agent.SMB1.BumpTakeAgent;
import kidridicarus.game.agent.SMB1.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB1.item.fireflower.FireFlower;
import kidridicarus.game.agent.SMB1.item.mushroom.MagicMushroom;
import kidridicarus.game.agent.SMB1.item.mushroom.Up1Mushroom;
import kidridicarus.game.agent.SMB1.item.powerstar.PowerStar;
import kidridicarus.game.agent.SMB1.other.brickpiece.BrickPiece;
import kidridicarus.game.agent.SMB1.other.floatingpoints.FloatingPoints;
import kidridicarus.game.agent.SMB1.other.spincoin.SpinCoin;
import kidridicarus.game.info.SMB1_Audio;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.SMB1_Pow;

public class BumpTile extends PlacedBoundsAgent implements TileBumpTakeAgent, DisposableAgent {
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
	private enum BlockItem { NONE, COIN, COIN10, MAGIC_MUSHROOM, UP1_MUSHROOM, STAR }

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
	private SolidTiledMapAgent solidTileMap; 
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

		isQblock = properties.containsKey(SMB1_KV.KEY_QBLOCK);

		blockItem = BlockItem.NONE;
		String spawnItem = properties.get(SMB1_KV.KEY_SPAWNITEM, "", String.class);
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
		isItemAvailable = blockItem != BlockItem.NONE;

		solidTileMap = null;

		body = new BumpTileBody(agency.getWorld(), this, AP_Tool.getBounds(properties));
		sprite = new BumpTileSprite(agency.getAtlas(), AP_Tool.getTexRegion(properties));

		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doRegularUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { doDraw(adBatch); }
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
				// Check for first contact with solid tile map every frame until found,
				// make the tile solid in the tile physics layer if it is not a secret block. 
				solidTileMap = body.getSpine().getSolidTileMap();
				if(solidTileMap != null &&
						!properties.containsKV(SMB1_KV.KEY_SECRETBLOCK, CommonKV.VAL_TRUE)) {
					solidTileMap.setTileSolidStateAtPos(body.getPosition(), true);
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
			if(properties.get(SMB1_KV.KEY_SECRETBLOCK, "", String.class).equals(CommonKV.VAL_TRUE))
				solidTileMap.setTileSolidStateAtPos(body.getPosition(), true);

			switch(blockItem) {
				case COIN:
					isItemAvailable = false;
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
			case UP1_MUSHROOM:
				agency.createAgent(Up1Mushroom.makeAP(pos));
				agency.getEar().playSound(SMB1_Audio.Sound.POWERUP_SPAWN);
				break;
			case MAGIC_MUSHROOM:
				// hard bump gives a fireflower, soft bump gives mushroom
				if(bumpStrength == TileBumpStrength.HARD)
					agency.createAgent(FireFlower.makeAP(pos));
				else
					agency.createAgent(MagicMushroom.makeAP(pos));
				agency.getEar().playSound(SMB1_Audio.Sound.POWERUP_SPAWN);
				break;
			case STAR:
				agency.createAgent(PowerStar.makeAP(pos));
				agency.getEar().playSound(SMB1_Audio.Sound.POWERUP_SPAWN);
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
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(right, up),
				new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(right, -up),
				new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(-right, up),
				new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(BrickPiece.makeAP(body.getPosition().cpy().add(-right, -up),
				new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));

		agency.getEar().playSound(SMB1_Audio.Sound.BREAK);
		agency.removeAgent(this);

		Powerup.tryPushPowerup(bumpingAgent, new SMB1_Pow.PointsPow(100));
	}

	private void startSpinningCoin() {
		agency.createAgent(FloatingPoints.makeAP(200, false, body.getPosition(), bumpingAgent));
		// spawn a coin one tile's height above the current tile position
		agency.createAgent(SpinCoin.makeAP(body.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y))));
		agency.getEar().playSound(SMB1_Audio.Sound.COIN);
		// push coin powerup to powerup take Agent if Agent exists
		Powerup.tryPushPowerup(bumpingAgent, new SMB1_Pow.CoinPow());
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

	private void doDraw(Eye adBatch) {
		adBatch.draw(sprite);
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
