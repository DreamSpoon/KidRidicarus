package com.ridicarus.kid.SpecialTiles;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.TileIDs;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.roles.robot.BounceCoin;
import com.ridicarus.kid.roles.robot.BumpableBot;
import com.ridicarus.kid.roles.robot.FireFlower;
import com.ridicarus.kid.roles.robot.PowerMushroom;
import com.ridicarus.kid.scenes.Hud;
import com.ridicarus.kid.tools.WorldRunner;

public class BumpableTile extends InteractiveTileObject {
	private static final float BOUNCE_TIME = 0.15f;
	private static final float BOUNCE_HEIGHT_FRAC = 0.2f;	// 1/5 of tile bounce height
	private static final float BREAKRIGHT_VEL1_X = 1f;
	private static final float BREAKRIGHT_VEL1_Y = 3f;
	private static final float BREAKRIGHT_VEL2_X = 1f;
	private static final float BREAKRIGHT_VEL2_Y = 4f;
	// bricks should be auto-removed when off screen, use this timeout for other cases
	private static final float BRICK_DIE_TIME = 7f;

	private static final float BLINK_FRAMETIME = 0.133f;
	private static final int[] BLINK_FRAMES = { TileIDs.ANIMQ_BLINK1, TileIDs.ANIMQ_BLINK2, TileIDs.ANIMQ_BLINK3,
			TileIDs.ANIMQ_BLINK2, TileIDs.ANIMQ_BLINK1, TileIDs.ANIMQ_BLINK1};

	public enum BrickState { STAND, BOUNCE, BROKEN, };
	private BrickState curState;
	private float stateTimer;

	private boolean isHit;
	private boolean isHitByBig;
	private float bounceTimeLeft;
	private boolean isBroken;
	private BrickPiece[] brickPieces;
	private Sprite brickSprite;
	private boolean isQblock;
	private int curBlinkFrame;

	private enum BrickItem { NONE, COIN, MUSHROOM };
	private BrickItem myItem;
	private boolean isItemAvailable;

	public BumpableTile(WorldRunner runner, MapObject object) {
		super(runner, object);

		isHit = false;
		isHitByBig = false;
		bounceTimeLeft = 0f;
		isBroken = false;
		brickPieces = null;
		curState = BrickState.STAND;
		stateTimer = 0f;
		isQblock = object.getProperties().containsKey(GameInfo.ANIM_QMARK_TILEKEY);
		curBlinkFrame = 0;

		if(object.getProperties().containsKey(GameInfo.COIN_TILEKEY))
			myItem = BrickItem.COIN;
		else if(object.getProperties().containsKey(GameInfo.MUSHROOM_TILEKEY))
			myItem = BrickItem.MUSHROOM;
		else 
			myItem = BrickItem.NONE;

		isItemAvailable = (myItem != BrickItem.NONE);

		fixture.setUserData(this);
		setCategoryAndMaskFilter(GameInfo.BANGABLE_BIT, GameInfo.MARIOHEAD_BIT);

		brickSprite = new Sprite(runner.getMap().getTileSets().getTile(myTileID).getTextureRegion());
		brickSprite.setPosition(GameInfo.P2M(bounds.getX()), GameInfo.P2M(bounds.getY()));
		brickSprite.setBounds(brickSprite.getX(), brickSprite.getY(), tileWidth, tileHeight);

		if(isQblock) {
			// animated question mark block, so it needs updates
			runner.enableInteractiveTileUpdates(this);
		}
	}

	private BrickState getState() {
		if(isBroken)
			return BrickState.BROKEN;
		else if(bounceTimeLeft > 0f)
			return BrickState.BOUNCE;
		else if(isHit) {
			if(myItem == BrickItem.NONE && isHitByBig)
				return BrickState.BROKEN;
			else if(myItem != BrickItem.NONE && !isItemAvailable)
				return BrickState.STAND;
			else
				return BrickState.BOUNCE;
		}
		else
			return BrickState.STAND;
	}

	@Override
	public void update(float delta) {
		BrickState prevState = curState;
		curState = getState();
		switch(curState) {
			case BOUNCE:
				// first time bounce?
				if(curState != prevState) {
					bopTopGoombas();
					bounceTimeLeft = BOUNCE_TIME;
					if(!isItemAvailable)
						MyKidRidicarus.manager.get(GameInfo.SOUND_BUMP, Sound.class).play();
					else if(myItem == BrickItem.COIN) {
						MyKidRidicarus.manager.get(GameInfo.SOUND_COIN, Sound.class).play();
						Hud.addScore(200);
						isItemAvailable = false;
						spawnSpinningCoin();
					}
					else if(myItem == BrickItem.MUSHROOM) {
						MyKidRidicarus.manager.get(GameInfo.SOUND_POWERUP_SPAWN, Sound.class).play();
						Hud.addScore(200);
						isItemAvailable = false;
						if(isHitByBig) {
							runner.addRobot(new FireFlower(runner, body.getPosition().x,
									body.getPosition().y + GameInfo.P2M(GameInfo.TILEPIX_Y)));
						}
						else {
							runner.addRobot(new PowerMushroom(runner, body.getPosition().x,
									body.getPosition().y + GameInfo.P2M(GameInfo.TILEPIX_Y)));
						}
					}
						
					hideMyTile();
					brickSprite.setPosition(body.getPosition().x - tileWidth/2, body.getPosition().y - tileHeight/2);
					break;
				}
				else {	// the bounce continues...
					bounceTimeLeft -= delta;
					// is bounce finishing?
					if(bounceTimeLeft <= 0f) {
						bounceTimeLeft = 0f;
						// if the brick contains items, but no more are available,...
						if(!isItemAvailable && myItem != BrickItem.NONE) {
							// then switch graphics to the "item used" block,...
							changeMyTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(TileIDs.COIN_EMPTY));
						}
						else {
							// otherwise return graphics to the original block
							unhideMyTile();
						}

						runner.disableInteractiveTileUpdates(this);

						stateTimer = 0f;
						curState = BrickState.STAND;
					}
					else {
						// linear bounce up to max height at halftime, then return down to original height at endtime
						float bounceHeight;
						// time to go up?
						if(bounceTimeLeft >= BOUNCE_TIME/2)
							bounceHeight = (BOUNCE_TIME-bounceTimeLeft) / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * tileHeight;
						else	// time to go down
							bounceHeight = bounceTimeLeft / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * tileHeight;
	
						brickSprite.setPosition(body.getPosition().x - tileWidth/2,
								body.getPosition().y - tileHeight/2 + bounceHeight);
					}
				}
				break;
			case BROKEN:
				// new break?
				if(prevState != curState) {
					MyKidRidicarus.manager.get(GameInfo.SOUND_BREAK, Sound.class).play();
					Hud.addScore(200);
					isBroken = true;
					startBreakBrick();
				}
				else
					continueBreakBrick(delta);

				break;
			case STAND:
				if(isQblock && isItemAvailable)
					setBlinkFrame();
				else
					runner.disableInteractiveTileUpdates(this);

				break;
		}

		stateTimer = curState == prevState ? stateTimer+delta : 0f;
		isHit = false;
		isHitByBig = false;
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
			setTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(BLINK_FRAMES[curBlinkFrame]));
	}

	private void startBreakBrick() {
		float right, up;
		Vector2 position;

		bopTopGoombas();

		// remove the tile graphic from the tilemap
		destroyTile();
		// remove the physics body
		position = body.getPosition();
		runner.getWorld().destroyBody(body);

		// create 4 brick pieces in the 4 corners of the original space and blast them upwards
		right = tileWidth / 4f;
		up = tileHeight / 4f;
		brickPieces = new BrickPiece[4];
		brickPieces[0] = new BrickPiece(runner, position.cpy().add(right, up), new Vector2(BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 0);
		brickPieces[1] = new BrickPiece(runner, position.cpy().add(right, -up), new Vector2(BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 1);
		brickPieces[2] = new BrickPiece(runner, position.cpy().add(-right, up), new Vector2(-BREAKRIGHT_VEL1_X, BREAKRIGHT_VEL1_Y), 2);
		brickPieces[3] = new BrickPiece(runner, position.cpy().add(-right, -up), new Vector2(-BREAKRIGHT_VEL2_X, BREAKRIGHT_VEL2_Y), 3);
	}

	private void continueBreakBrick(float delta) {
		brickPieces[0].update(delta);
		brickPieces[1].update(delta);
		brickPieces[2].update(delta);
		brickPieces[3].update(delta);

		// check if all brick pieces fell off the bottom of the screen, or if they timed out
		if((brickPieces[0].getPosition().y < 0f && brickPieces[1].getPosition().y < 0f &&
			brickPieces[2].getPosition().y < 0f && brickPieces[3].getPosition().y < 0f ) || stateTimer > BRICK_DIE_TIME)
			runner.destroyInteractiveTile(this);
	}

	private void bopTopGoombas() {
		// use QueryAABB to build robotsOnMe list
		final ArrayList<RobotRole> robotsOnMe;

		robotsOnMe = new ArrayList<RobotRole>();
		// check for robots in an area slightly thinner than the tile, and only as tall as the tile bounces
		// (shrink the box a bit so we don't get enemies on adjacent tiles - TODO: fix this bug!)
		runner.getWorld().QueryAABB(
				new QueryCallback() {
					@Override
					public boolean reportFixture(Fixture fixture) {
						if(fixture.getUserData() instanceof RobotRole &&
								(fixture.getFilterData().categoryBits & (GameInfo.ROBOT_BIT | GameInfo.ITEM_BIT)) != 0) {
							robotsOnMe.add((RobotRole) fixture.getUserData()); 
						}
						return true;
					}
				}, body.getPosition().x - tileWidth/2f*0.25f, body.getPosition().y + tileHeight/2f,
				body.getPosition().x + tileWidth/2f*0.25f,
				body.getPosition().y + tileHeight/2f + tileHeight*BOUNCE_HEIGHT_FRAC);

		// bop any goombas/turtles that are standing on the brick
		Iterator<RobotRole> iter = robotsOnMe.iterator();
		while(iter.hasNext()) {
			RobotRole robot = iter.next();
			if(robot instanceof BumpableBot)
				((BumpableBot) robot).onBump(body.getPosition());
		}
	}

	@Override
	public void draw(Batch batch) {
		if(brickPieces != null) {
			brickPieces[0].draw(batch);
			brickPieces[1].draw(batch);
			brickPieces[2].draw(batch);
			brickPieces[3].draw(batch);
		}
		else {
			if(curState == BrickState.BOUNCE)
				brickSprite.draw(batch);
		}
	}

	@Override
	public void onHeadHit(PlayerRole player) {
		if(player instanceof MarioRole) {
			// only one head hit per update, and only while not bouncing
			if(!isHit && bounceTimeLeft == 0f) {
				isHit = true;
				isHitByBig = ((MarioRole) player).isBig();
				runner.enableInteractiveTileUpdates(this);
			}
		}
	}

	@Override
	public void destroy() {
		if(body != null)
			runner.getWorld().destroyBody(body);
		body = null;
	}
}
