package kidridicarus.roles.robot.SMB;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.SMB.BumpTileBody;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.SMBInfo.PointAmount;
import kidridicarus.info.UInfo;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.BumpableBot;
import kidridicarus.sprites.SMB.BumpTileSprite;
import kidridicarus.tools.RRDefFactory;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class BumpTile implements RobotRole, BumpableBot {
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

	private MapProperties properties;
	private RoleWorld runner;
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
	private PlayerRole prhead;
	private boolean wasHitByBig;

	public BumpTile(RoleWorld runner, RobotRoleDef rdef) {
		this.runner = runner;
		properties = rdef.properties;

		isHit = false;
		prhead = null;
		wasHitByBig = false;
		curState = BumpState.PREBUMP;
		stateTimer = 0f;

		coin10Coins = 10;
		coin10BumpResetTimer = 0;
		coin10EndTimer = 0;

		isQ = false;
		if(rdef.properties.containsKey(KVInfo.KEY_QBLOCK))
			isQ = true;

		blockItem = BlockItem.NONE;
		if(rdef.properties.containsKey(KVInfo.KEY_SPAWNITEM)) {
			String spawnItem = rdef.properties.get(KVInfo.KEY_SPAWNITEM, String.class);
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
			else if(spawnItem.equals(KVInfo.VAL_STAR))
				blockItem = BlockItem.STAR;
			else if(spawnItem.equals(KVInfo.VAL_MUSH1UP))
				blockItem = BlockItem.MUSH1UP;
		}
		isItemAvailable = blockItem != BlockItem.NONE;

		itbody = new BumpTileBody(runner.getWorld(), this, rdef.bounds);

		btsprite = new BumpTileSprite(runner.getEncapTexAtlas(), rdef.tileTexRegion);

		// make the tile solid in the tile physics layer
		runner.setPhysicTile(UInfo.getM2PTileForPos(itbody.getPosition()), true);

		runner.setRobotDrawLayer(this, SpriteDrawOrder.MIDDLE);
		runner.enableRobotUpdate(this);
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

	public void onBounceStart() {
		if(blockItem == BlockItem.NONE && wasHitByBig)
			startBreakBrick();
		else {
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

	public void onBounceEnd() {
		Vector2 pos = itbody.getPosition().cpy().add(0f, UInfo.P2M(UInfo.TILEPIX_Y));
		switch(blockItem) {
			case MUSH1UP:
				runner.playSound(AudioInfo.SOUND_POWERUP_SPAWN);
				runner.createRobot(RRDefFactory.makeMushroom1UPDef(pos));
				break;
			case MUSHROOM:
				runner.playSound(AudioInfo.SOUND_POWERUP_SPAWN);
				// big mario pops a fireflower?
				if(wasHitByBig)
					runner.createRobot(RRDefFactory.makeFireFlowerDef(pos));
				else
					runner.createRobot(RRDefFactory.makePowerMushroomDef(pos));
				break;
			case STAR:
				runner.playSound(AudioInfo.SOUND_POWERUP_SPAWN);
				runner.createRobot(RRDefFactory.makePowerMushroomDef(pos));
				break;
			default:
				break;
		}
	}

	private void startBreakBrick() {
		runner.playSound(AudioInfo.SOUND_BREAK);
		runner.createRobot(RRDefFactory.makeFloatingPointsDef(PointAmount.P200, false,
				itbody.getPosition(), UInfo.P2M(UInfo.TILEPIX_Y * 2), (MarioRole) prhead));
		runner.setPhysicTile(UInfo.getM2PTileForPos(itbody.getPosition()), false);

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		float right = itbody.getBounds().width / 4f;
		float up = itbody.getBounds().height / 4f;

		// replace the tile with 4 brick pieces shooting upward and outward
		runner.createRobot(RRDefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(right, up),
				new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		runner.createRobot(RRDefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(right, -up),
				new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		runner.createRobot(RRDefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(-right, up),
				new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		runner.createRobot(RRDefFactory.makeBrickPieceDef(itbody.getPosition().cpy().add(-right, -up),
				new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 0));
		runner.destroyRobot(this);
	}

	private void startSpinningCoin() {
		runner.playSound(AudioInfo.SOUND_COIN);
		runner.createRobot(RRDefFactory.makeFloatingPointsDef(PointAmount.P200, false, itbody.getPosition(),
				UInfo.P2M(UInfo.TILEPIX_Y * 2), (MarioRole) prhead));

		// spawn a coin one tile's height above the current tile position
		runner.createRobot(RRDefFactory.makeSpinCoinDef(itbody.getPosition().cpy().add(0f,
				UInfo.P2M(UInfo.TILEPIX_Y))));
	}

	@Override
	public void draw(Batch batch) {
		btsprite.draw(batch);
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		isHit = true;
		prhead = perp;
		if(perp instanceof MarioRole && ((MarioRole) perp).isBig())
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
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		itbody.dispose();
	}
}
