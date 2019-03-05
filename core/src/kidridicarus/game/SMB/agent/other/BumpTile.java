package kidridicarus.game.SMB.agent.other;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.game.SMB.agent.BumpTakeAgent;
import kidridicarus.game.SMB.agent.TileBumpTakeAgent;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agentbody.other.BumpTileBody;
import kidridicarus.game.SMB.agentsprite.other.BumpTileSprite;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.SMBInfo.PointAmount;

public class BumpTile extends Agent implements UpdatableAgent, DrawableAgent, TileBumpTakeAgent {
	private static final float BOUNCE_TIME = 0.175f;
	private static final float BOUNCE_HEIGHT_FRAC = 0.225f;	// bounce up about 1/5 of tile height

	private static final float BREAKRIGHT_VEL1_X = 1f;
	private static final float BREAKRIGHT_VEL1_Y = 3f;
	private static final float BREAKRIGHT_VEL2_X = 1f;
	private static final float BREAKRIGHT_VEL2_Y = 4f;

	private static final float COIN_BUMP_RESET_TIME = 0.23f;
	private static final float MAX_COIN_BUMP_TIME = 3f;

	// only blocks with items can reach the empty state, other blocks only use pre- and mid-bump
	public enum BumpState { PREBUMP, MIDBUMP, EMPTY }
	public enum BlockItem { NONE, COIN, COIN10, MUSHROOM, STAR, MUSH1UP }

	private BumpTileBody btBody;
	private BumpTileSprite btSprite;

	private boolean isQ;
	private boolean isHit;
	private boolean isItemAvailable;
	private BlockItem blockItem;
	private int coin10Coins;
	private float coin10BumpResetTimer;
	private float coin10EndTimer;

	private BumpState curState;
	private float stateTimer;
	private Agent bumpingAgent;
	private boolean wasHitByBig;

	public BumpTile(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isHit = false;
		bumpingAgent = null;
		wasHitByBig = false;
		curState = BumpState.PREBUMP;
		stateTimer = 0f;

		coin10Coins = 10;
		coin10BumpResetTimer = 0;
		coin10EndTimer = 0;

		isQ = properties.containsKey(GameKV.SMB.KEY_QBLOCK);

		blockItem = BlockItem.NONE;
		String spawnItem = properties.get(GameKV.SMB.KEY_SPAWNITEM, "", String.class);
		if(spawnItem.equals(GameKV.SMB.VAL_COIN))
			blockItem = BlockItem.COIN;
		else if(spawnItem.equals(GameKV.SMB.VAL_COIN10)) {
			blockItem = BlockItem.COIN10;
			coin10Coins = 10;
			coin10BumpResetTimer = 0f;
			coin10EndTimer = 0f;
		}
		else if(spawnItem.equals(GameKV.SMB.VAL_MUSHROOM))
			blockItem = BlockItem.MUSHROOM;
		else if(spawnItem.equals(GameKV.SMB.VAL_POWERSTAR))
			blockItem = BlockItem.STAR;
		else if(spawnItem.equals(GameKV.SMB.VAL_MUSH1UP))
			blockItem = BlockItem.MUSH1UP;
		isItemAvailable = blockItem != BlockItem.NONE;

		btBody = new BumpTileBody(agency.getWorld(), this, Agent.getStartBounds(properties));
		btSprite = new BumpTileSprite(agency.getAtlas(), Agent.getStartTexRegion(properties));

		// make the tile solid in the tile physics layer if it is not a secret block 
		if(!properties.containsKV(GameKV.SMB.KEY_SECRETBLOCK, AgencyKV.VAL_TRUE))
			agency.setPhysicTile(UInfo.getM2PTileForPos(btBody.getPosition()), true);

		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
		agency.enableAgentUpdate(this);
	}

	private BumpState getState() {
		if(curState == BumpState.EMPTY)
			return BumpState.EMPTY;
		else if(curState == BumpState.MIDBUMP) {
			if(stateTimer <= BOUNCE_TIME)
				return BumpState.MIDBUMP;
			else if(blockItem != BlockItem.NONE && !isItemAvailable)
				return BumpState.EMPTY;
		}
		else if(isHit)
			return BumpState.MIDBUMP;
		return BumpState.PREBUMP;
	}

	@Override
	public void update(float delta) {
		float offsetY = 0f;
		BumpState nextState = getState();
		switch(nextState) {
			case PREBUMP:
			case EMPTY:
				// last frame was midbump?
				if(curState == BumpState.MIDBUMP)
					onBounceEnd();
				break;
			case MIDBUMP:
				// first frame of midbump?
				if(curState != nextState)
					onBounceStart();
				else {
					// linear bounce up to max height at halftime, then return down to original height at endtime
					// time to go up?
					if(stateTimer <= BOUNCE_TIME/2)
						offsetY = stateTimer / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * btBody.getBounds().height;
					else	// time to go down
						offsetY = (BOUNCE_TIME-stateTimer) / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * btBody.getBounds().height;
				}
				break;
		}

		boolean isEmpty = !isItemAvailable && blockItem != BlockItem.NONE;
		btSprite.update(delta, agency.getGlobalTimer(), btBody.getPosition().add(0f,  offsetY), isQ, isEmpty);

		isHit = false;
		if(blockItem == BlockItem.COIN10) {
			if(coin10BumpResetTimer >= delta)
				coin10BumpResetTimer -= delta;
			else
				coin10BumpResetTimer = 0f;

			if(coin10EndTimer > 0f)
				coin10EndTimer -= delta;
		}

		stateTimer = nextState == curState ? stateTimer+delta : 0f;
		curState = nextState;
	}

	private void onBounceStart() {
		bopTopGoombas();

		if(blockItem == BlockItem.NONE && wasHitByBig)
			startBreakBrick();
		else {
			// if the tile was a secret block then it was not solid, so make it solid 
			if(properties.get(GameKV.SMB.KEY_SECRETBLOCK, "", String.class).equals(AgencyKV.VAL_TRUE))
				agency.setPhysicTile(UInfo.getM2PTileForPos(btBody.getPosition()), true);

			switch(blockItem) {
				case COIN:
					isItemAvailable = false;
					if(bumpingAgent instanceof Mario)
						((Mario) bumpingAgent).giveCoin();
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

					if(bumpingAgent instanceof Mario)
						((Mario) bumpingAgent).giveCoin();
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
				}, btBody.getPosition().x - btBody.getBounds().width/2f*0.25f, btBody.getPosition().y + btBody.getBounds().height/2f,
				btBody.getPosition().x + btBody.getBounds().width/2f*0.25f,
				btBody.getPosition().y + btBody.getBounds().height/2f + btBody.getBounds().height*BOUNCE_HEIGHT_FRAC);

		// bop any bumpable agents that are standing on the tile
		Iterator<Agent> iter = agentsOnMe.iterator();
		while(iter.hasNext()) {
			Agent agent = iter.next();
			if(agent instanceof BumpTakeAgent)
				((BumpTakeAgent) agent).onBump(bumpingAgent);
		}
	}

	private void onBounceEnd() {
		Vector2 pos = btBody.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y));
		switch(blockItem) {
			case MUSH1UP:
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_SPAWN);
				agency.createAgent(Agent.createPointAP(GameKV.SMB.VAL_MUSH1UP, pos));
				break;
			case MUSHROOM:
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_SPAWN);
				// big mario pops a fireflower?
				if(wasHitByBig)
					agency.createAgent(Agent.createPointAP(GameKV.SMB.VAL_FIREFLOWER, pos));
				else
					agency.createAgent(Agent.createPointAP(GameKV.SMB.VAL_MUSHROOM, pos));
				break;
			case STAR:
				agency.playSound(AudioInfo.Sound.SMB.POWERUP_SPAWN);
				agency.createAgent(Agent.createPointAP(GameKV.SMB.VAL_POWERSTAR, pos));
				break;
			default:
				break;
		}
	}

	private void startBreakBrick() {
		agency.playSound(AudioInfo.Sound.SMB.BREAK);
		((Mario) bumpingAgent).givePoints(PointAmount.P200, false);
		agency.setPhysicTile(UInfo.getM2PTileForPos(btBody.getPosition()), false);

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		float right = btBody.getBounds().width / 4f;
		float up = btBody.getBounds().height / 4f;
		// replace the tile with 4 brick pieces shooting upward and outward
		agency.createAgent(BrickPiece.makeAP(btBody.getPosition().cpy().add(right, up),
				new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(BrickPiece.makeAP(btBody.getPosition().cpy().add(right, -up),
				new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		agency.createAgent(BrickPiece.makeAP(btBody.getPosition().cpy().add(-right, up),
				new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(BrickPiece.makeAP(btBody.getPosition().cpy().add(-right, -up),
				new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		agency.disposeAgent(this);
	}

	private void startSpinningCoin() {
		agency.playSound(AudioInfo.Sound.SMB.COIN);
		agency.createAgent(FloatingPoints.makeAP(PointAmount.P200, false, btBody.getPosition(),
				UInfo.P2M(UInfo.TILEPIX_Y * 2f), (Mario) bumpingAgent));

		// spawn a coin one tile's height above the current tile position
		agency.createAgent(Agent.createPointAP(GameKV.SMB.VAL_SPINCOIN, btBody.getPosition().cpy().add(0f,
				UInfo.P2M(UInfo.TILEPIX_Y))));
	}

	@Override
	public void draw(Batch batch) {
		btSprite.draw(batch);
	}

	@Override
	public void onBumpTile(Agent bumpingAgent) {
		isHit = true;
		this.bumpingAgent = bumpingAgent;
		if(bumpingAgent instanceof Mario && ((Mario) bumpingAgent).isBig())
			wasHitByBig = true;
	}

	@Override
	public Vector2 getPosition() {
		return btBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return btBody.getBounds();
	}

	@Override
	public void dispose() {
		btBody.dispose();
	}
}
