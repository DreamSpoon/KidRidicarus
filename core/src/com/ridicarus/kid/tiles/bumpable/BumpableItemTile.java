package com.ridicarus.kid.tiles.bumpable;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.robot.BounceCoin;
import com.ridicarus.kid.roles.robot.BrickPiece;
import com.ridicarus.kid.roles.robot.FireFlower;
import com.ridicarus.kid.roles.robot.PowerStar;
import com.ridicarus.kid.roles.robot.PowerMushroom;
import com.ridicarus.kid.scenes.Hud;
import com.ridicarus.kid.tiles.TileIDs;
import com.ridicarus.kid.tools.WorldRunner;

public class BumpableItemTile extends BumpableTile {
	private static final float BREAKRIGHT_VEL1_X = 1f;
	private static final float BREAKRIGHT_VEL1_Y = 3f;
	private static final float BREAKRIGHT_VEL2_X = 1f;
	private static final float BREAKRIGHT_VEL2_Y = 4f;

	private static final float BLINK_FRAMETIME = 0.133f;
	private static final int[] BLINK_FRAMES = { TileIDs.ANIMQ_BLINK1, TileIDs.ANIMQ_BLINK2, TileIDs.ANIMQ_BLINK3,
			TileIDs.ANIMQ_BLINK2, TileIDs.ANIMQ_BLINK1, TileIDs.ANIMQ_BLINK1};

	private static final float COIN_BUMP_RESET_TIME = 0.23f;
	private static final float MAX_COIN_BUMP_TIME = 3f;

	private boolean isQblock;
	private int curBlinkFrame;

	// in a way, the Brick also has an item not included here: Explode Into Pieces - an animation without a powerup
	private enum BrickItem { NONE, COIN, COIN10, MUSHROOM, STAR };
	private BrickItem myItem;
	private boolean isItemAvailable;

	private float stateTimer;

	private int coin10Coins;
	private float coin10BumpResetTimer;
	private float coin10EndTimer;

	public BumpableItemTile(WorldRunner runner, MapObject object) {
		super(runner, object);

		isQblock = object.getProperties().containsKey(GameInfo.ANIM_QMARK_TILEKEY);
		curBlinkFrame = 0;

		if(object.getProperties().containsKey(GameInfo.COIN_TILEKEY)) {
			myItem = BrickItem.COIN;
			// switch to empty block as soon as bump starts
			setBounceImage(runner.getMap().getTileSets().getTile(TileIDs.COIN_EMPTY).getTextureRegion());
		}
		else if(object.getProperties().containsKey(GameInfo.COIN10_TILEKEY)) {
			myItem = BrickItem.COIN10;
			coin10Coins = 10;
			coin10BumpResetTimer = 0f;
			coin10EndTimer = 0f;
			// do not switch to empty block as soon as bump starts, retain original image
		}
		else if(object.getProperties().containsKey(GameInfo.MUSHROOM_TILEKEY)) {
			myItem = BrickItem.MUSHROOM;
			// switch to empty block as soon as bump starts
			setBounceImage(runner.getMap().getTileSets().getTile(TileIDs.COIN_EMPTY).getTextureRegion());
		}
		else if(object.getProperties().containsKey(GameInfo.STAR_TILEKEY)) {
			myItem = BrickItem.STAR;
			// switch to empty block as soon as bump starts
			setBounceImage(runner.getMap().getTileSets().getTile(TileIDs.COIN_EMPTY).getTextureRegion());
		}
		else
			myItem = BrickItem.NONE;

		isItemAvailable = (myItem != BrickItem.NONE);

		// animated question mark blocks need updates
		if(isQblock)
			runner.enableInteractiveTileUpdates(this);

		stateTimer = 0f;
	}

	@Override
	public void onBump(boolean isBig) {
		// the brick makes a bump sound unless it is breaking
		if(!(isBig && myItem == BrickItem.NONE))
			runner.playSound(GameInfo.SOUND_BUMP);
	}

	@Override
	public void onBounceStart(boolean isHitByBig) {
		if(!isItemAvailable) {
			if(isHitByBig) {
				runner.playSound(GameInfo.SOUND_BREAK);
				Hud.addScore(200);
				startBreakBrick();
			}
		}
		else {
			switch(myItem) {
				case COIN:
					isItemAvailable = false;
					runner.playSound(GameInfo.SOUND_COIN);
					Hud.addScore(200);
					spawnSpinningCoin();
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

					runner.playSound(GameInfo.SOUND_COIN);
					Hud.addScore(200);
					spawnSpinningCoin();
					break;
				case MUSHROOM:
					isItemAvailable = false;
					runner.playSound(GameInfo.SOUND_POWERUP_SPAWN);
					Hud.addScore(200);
					// big mario pops a fireflower?
					if(isHitByBig) {
						runner.addRobot(new FireFlower(runner, body.getPosition().cpy().add(0f, GameInfo.P2M(GameInfo.TILEPIX_Y))));
					}
					else {
						runner.addRobot(new PowerMushroom(runner, body.getPosition().cpy().add(0f, GameInfo.P2M(GameInfo.TILEPIX_Y))));
					}
					break;
				case STAR:
					isItemAvailable = false;
					runner.playSound(GameInfo.SOUND_POWERUP_SPAWN);
					Hud.addScore(200);
					runner.addRobot(new PowerStar(runner, body.getPosition().cpy().add(0f, GameInfo.P2M(GameInfo.TILEPIX_Y))));
					break;
				// this _should_ be unnecessary since the item can't be NONE at this point
				default:
					break;
			}
		}
	}

	@Override
	public void onBounceEnd() {
		// If the brick contains items, but no more are available, then switch image to the
		// "item used" block and disable bumps
		if(!isItemAvailable && myItem != BrickItem.NONE) {
			setBounceEnabled(false);
			setImageTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(TileIDs.COIN_EMPTY));
		}
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		stateTimer += delta;
		if(isMidBounce())
			return;

		if(myItem == BrickItem.COIN10) {
			if(coin10BumpResetTimer >= delta)
				coin10BumpResetTimer -= delta;
			else
				coin10BumpResetTimer = 0f;

			if(coin10EndTimer > 0f)
				coin10EndTimer -= delta;
		}

		if(isQblock && isItemAvailable)
			setBlinkFrame();

		// continue updates until both timers reach zero
		if(myItem == BrickItem.COIN10 && coin10BumpResetTimer <= 0f && this.coin10EndTimer <= 0f)
			runner.disableInteractiveTileUpdates(this);
	}

	private void spawnSpinningCoin() {
		// spawn a coin one tile's height above the current tile position
		runner.addRobot(new BounceCoin(runner, body.getPosition().cpy().add(0f, GameInfo.P2M(GameInfo.TILEPIX_Y))));
	}

	private void setBlinkFrame() {
		int oldFrame;
		oldFrame = curBlinkFrame;
		curBlinkFrame = Math.floorMod((int) (stateTimer / BLINK_FRAMETIME), BLINK_FRAMES.length);
		// change the tile's graphic only if necessary
		if(curBlinkFrame != oldFrame)
			setImageTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(BLINK_FRAMES[curBlinkFrame]));
	}

	private void startBreakBrick() {
		float right, up;
		Vector2 position;

		setPhysicTile(false);
		setImageTile(null);

		// remove the physics body
		position = body.getPosition();

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		right = tileWidth / 4f;
		up = tileHeight / 4f;

		// replace the tile with 4 brick pieces shooting upward and outward
		runner.addRobot(new BrickPiece(runner, position.cpy().add(right, up), new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0));
		runner.addRobot(new BrickPiece(runner, position.cpy().add(right, -up), new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 1));
		runner.addRobot(new BrickPiece(runner, position.cpy().add(-right, up), new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 2));
		runner.addRobot(new BrickPiece(runner, position.cpy().add(-right, -up), new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 3));

		runner.destroyInteractiveTile(this);
	}
}
