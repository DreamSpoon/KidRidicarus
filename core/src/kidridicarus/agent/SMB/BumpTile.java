package kidridicarus.agent.SMB;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.bodies.SMB.BumpTileBody;
import kidridicarus.agent.optional.BumpableAgent;
import kidridicarus.agent.sprites.SMB.BumpTileSprite;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;

public class BumpTile extends Agent implements BumpableAgent, Disposable {
	private static final float BOUNCE_TIME = 0.175f;
	private static final float BOUNCE_HEIGHT_FRAC = 0.225f;	// bounce up about 1/5 of tile height

	private static final float BREAKRIGHT_VEL1_X = 1f;
	private static final float BREAKRIGHT_VEL1_Y = 3f;
	private static final float BREAKRIGHT_VEL2_X = 1f;
	private static final float BREAKRIGHT_VEL2_Y = 4f;

	private static final float COIN_BUMP_RESET_TIME = 0.23f;
	private static final float MAX_COIN_BUMP_TIME = 3f;

	// only blocks with items can reach the empty state, other blocks only use pre- and mid-bump
	public enum BumpState { PREBUMP, MIDBUMP, EMPTY };
	public enum BlockItem { NONE, COIN, COIN10, MUSHROOM, STAR, MUSH1UP };

	private BumpTileBody itbody;
	private BumpTileSprite btsprite;

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

	public BumpTile(Agency agency, AgentDef adef) {
		super(agency, adef);

		isHit = false;
		bumpingAgent = null;
		wasHitByBig = false;
		curState = BumpState.PREBUMP;
		stateTimer = 0f;

		coin10Coins = 10;
		coin10BumpResetTimer = 0;
		coin10EndTimer = 0;

		isQ = false;
		if(adef.properties.containsKey(KVInfo.KEY_QBLOCK))
			isQ = true;

		blockItem = BlockItem.NONE;
		if(adef.properties.containsKey(KVInfo.KEY_SPAWNITEM)) {
			String spawnItem = adef.properties.get(KVInfo.KEY_SPAWNITEM, String.class);
			if(spawnItem.equals(KVInfo.VAL_COIN))
				blockItem = BlockItem.COIN;
			else if(spawnItem.equals(KVInfo.VAL_COIN10)) {
				blockItem = BlockItem.COIN10;
				coin10Coins = 10;
				coin10BumpResetTimer = 0f;
				coin10EndTimer = 0f;
			}
			else if(spawnItem.equals(KVInfo.VAL_MUSHROOM))
				blockItem = BlockItem.MUSHROOM;
			else if(spawnItem.equals(KVInfo.VAL_POWERSTAR))
				blockItem = BlockItem.STAR;
			else if(spawnItem.equals(KVInfo.VAL_MUSH1UP))
				blockItem = BlockItem.MUSH1UP;
		}
		isItemAvailable = blockItem != BlockItem.NONE;

		itbody = new BumpTileBody(agency.getWorld(), this, adef.bounds);

		btsprite = new BumpTileSprite(agency.getEncapTexAtlas(), adef.tileTexRegion);

		// make the tile solid in the tile physics layer if it is not a secret block 
		if(!adef.properties.get(KVInfo.KEY_SECRETBLOCK, "", String.class).equals(KVInfo.VAL_TRUE))
			agency.setPhysicTile(UInfo.getM2PTileForPos(itbody.getPosition()), true);

		agency.setAgentDrawLayer(this, SpriteDrawOrder.MIDDLE);
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
						offsetY = stateTimer / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * itbody.getBounds().height;
					else	// time to go down
						offsetY = (BOUNCE_TIME-stateTimer) / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * itbody.getBounds().height;
				}
				break;
		}

		boolean isEmpty = !isItemAvailable && blockItem != BlockItem.NONE;
		btsprite.update(delta, itbody.getPosition().add(0f,  offsetY), isQ, isEmpty);

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
			if(properties.get(KVInfo.KEY_SECRETBLOCK, "", String.class).equals(KVInfo.VAL_TRUE))
				agency.setPhysicTile(UInfo.getM2PTileForPos(itbody.getPosition()), true);

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
		final ArrayList<AgentBody> agentsOnMe;

		agentsOnMe = new ArrayList<AgentBody>();
		// check for agents in an area slightly thinner than the tile, and only as tall as the tile bounces
		// (shrink the box a bit so we don't get enemies on adjacent tiles -
		// TODO: find a more accurate QueryAABB method)
		agency.getWorld().QueryAABB(
				new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {
						if(fixture.getUserData() instanceof AgentBody &&
								(fixture.getFilterData().categoryBits & (GameInfo.AGENT_BIT | GameInfo.ITEM_BIT)) != 0) {
							agentsOnMe.add((AgentBody) fixture.getUserData()); 
						}
						return true;
					}
				}, itbody.getPosition().x - itbody.getBounds().width/2f*0.25f, itbody.getPosition().y + itbody.getBounds().height/2f,
				itbody.getPosition().x + itbody.getBounds().width/2f*0.25f,
				itbody.getPosition().y + itbody.getBounds().height/2f + itbody.getBounds().height*BOUNCE_HEIGHT_FRAC);

		// bop any goombas/turtles that are standing on the brick
		Iterator<AgentBody> iter = agentsOnMe.iterator();
		while(iter.hasNext()) {
			AgentBody agentBody = iter.next();
			if(agentBody.getParent() instanceof BumpableAgent)
				((BumpableAgent) agentBody.getParent()).onBump(bumpingAgent, itbody.getPosition());
		}
	}

	private void onBounceEnd() {
		Vector2 pos = itbody.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y));
		switch(blockItem) {
			case MUSH1UP:
				agency.playSound(AudioInfo.SOUND_POWERUP_SPAWN);
				agency.createAgent(ADefFactory.makeMushroom1UPDef(pos));
				break;
			case MUSHROOM:
				agency.playSound(AudioInfo.SOUND_POWERUP_SPAWN);
				// big mario pops a fireflower?
				if(wasHitByBig)
					agency.createAgent(ADefFactory.makeFireFlowerDef(pos));
				else
					agency.createAgent(ADefFactory.makePowerMushroomDef(pos));
				break;
			case STAR:
				agency.playSound(AudioInfo.SOUND_POWERUP_SPAWN);
				agency.createAgent(ADefFactory.makePowerStarDef(pos));
				break;
			default:
				break;
		}
	}

	private void startBreakBrick() {
		agency.playSound(AudioInfo.SOUND_BREAK);
		((Mario) bumpingAgent).givePoints(PointAmount.P200, false);
		agency.setPhysicTile(UInfo.getM2PTileForPos(itbody.getPosition()), false);

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		float right = itbody.getBounds().width / 4f;
		float up = itbody.getBounds().height / 4f;

		// replace the tile with 4 brick pieces shooting upward and outward
		agency.createAgent(ADefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(right, up),
				new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(ADefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(right, -up),
				new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		agency.createAgent(ADefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(-right, up),
				new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		agency.createAgent(ADefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(-right, -up),
				new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		agency.disposeAgent(this);
	}

	private void startSpinningCoin() {
		agency.playSound(AudioInfo.SOUND_COIN);
		agency.createAgent(ADefFactory.makeFloatingPointsDef(PointAmount.P200, false, itbody.getPosition(),
				UInfo.P2M(UInfo.TILEPIX_Y * 2), (Mario) bumpingAgent));

		// spawn a coin one tile's height above the current tile position
		agency.createAgent(ADefFactory.makeSpinCoinDef(itbody.getPosition().cpy().add(0f,
				UInfo.P2M(UInfo.TILEPIX_Y))));
	}

	@Override
	public void draw(Batch batch) {
		btsprite.draw(batch);
	}

	@Override
	public void onBump(Agent bumpingAgent, Vector2 fromCenter) {
		isHit = true;
		this.bumpingAgent = bumpingAgent;
		if(bumpingAgent instanceof Mario && ((Mario) bumpingAgent).isBig())
			wasHitByBig = true;
	}

	@Override
	public Vector2 getPosition() {
		return itbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return itbody.getBounds();
	}

	@Override
	public void dispose() {
		itbody.dispose();
	}
}
