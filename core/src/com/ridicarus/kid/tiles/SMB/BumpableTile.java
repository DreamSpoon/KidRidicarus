package com.ridicarus.kid.tiles.SMB;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.player.MarioRole;
import com.ridicarus.kid.roles.robot.BumpableBot;
import com.ridicarus.kid.tiles.InteractiveTileObject;
import com.ridicarus.kid.worldrunner.WorldRunner;

/*
 * this class is abstract because it cannot be destroyed by big mario TODO: change this situation?
 * 
 * Bump vs Bounce:
 *  bump - mario's head hit the bottom of the block
 *  bounce - a bump occurred, and the block "bounced" upwards from the impact 
 */
public abstract class BumpableTile extends InteractiveTileObject {
	private static final float BOUNCE_TIME = 0.175f;
	private static final float BOUNCE_HEIGHT_FRAC = 0.225f;	// bounce up about 1/5 of tile height

	private boolean isHit;
	private PlayerRole prHead;
	private boolean isHitByBig;
	private boolean isBounceEnabled;
	private float bounceTimeLeft;
	private Sprite bounceSprite;

	public BumpableTile(WorldRunner runner, MapObject object) {
		super(runner, object);

		isHit = false;
		prHead = null;
		isHitByBig = false;
		isBounceEnabled = true;
		bounceTimeLeft = 0f;

		fixture.setUserData(this);
		setCategoryAndMaskFilter(GameInfo.BANGABLE_BIT, GameInfo.MARIOHEAD_BIT);

		// the default bounce sprite is the original tile image 
		bounceSprite = new Sprite(runner.getMap().getTileSets().getTile(myTileID).getTextureRegion());
		bounceSprite.setPosition(GameInfo.P2M(bounds.getX()), GameInfo.P2M(bounds.getY()));
		bounceSprite.setBounds(bounceSprite.getX(), bounceSprite.getY(), tileWidth, tileHeight);
	}

	@Override
	public void update(float delta) {
		// first time bounce? (cannot be bounced again while already bouncing)
		if(bounceTimeLeft == 0f && isHit) {
			onBump(isHitByBig, prHead);
			if(isBounceEnabled) {
				bopTopGoombas();
				bounceTimeLeft = BOUNCE_TIME;
				onBounceStart(isHitByBig, prHead);

				// replace the regular tile with the bounce sprite
				setImageTile(null);
				bounceSprite.setPosition(body.getPosition().x - tileWidth/2, body.getPosition().y - tileHeight/2);
			}
		}
		else if(bounceTimeLeft > 0f) {	// the bounce continues...
			bounceTimeLeft -= delta;
			// is bounce finishing?
			if(bounceTimeLeft <= 0f) {
				bounceTimeLeft = 0f;

				// by default, reset image to the original block graphic
				setImageTile(runner.getMap().getTileSets().getTileSet(GameInfo.TILESET_GUTTER).getTile(myTileID));

				// onBounceEnd will need to decide whether or not to disable tile updates
				onBounceEnd();
			}
			else {
				// linear bounce up to max height at halftime, then return down to original height at endtime
				float bounceHeight;
				// time to go up?
				if(bounceTimeLeft >= BOUNCE_TIME/2)
					bounceHeight = (BOUNCE_TIME-bounceTimeLeft) / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * tileHeight;
				else	// time to go down
					bounceHeight = bounceTimeLeft / (BOUNCE_TIME/2) * BOUNCE_HEIGHT_FRAC * tileHeight;

				bounceSprite.setPosition(body.getPosition().x - tileWidth/2,
						body.getPosition().y - tileHeight/2 + bounceHeight);
			}
		}

		isHit = isHitByBig = false;
		prHead = null;
	}

	private void bopTopGoombas() {
		// use QueryAABB to build robotsOnMe list
		final ArrayList<RobotRole> robotsOnMe;

		robotsOnMe = new ArrayList<RobotRole>();
		// check for robots in an area slightly thinner than the tile, and only as tall as the tile bounces
		// (shrink the box a bit so we don't get enemies on adjacent tiles -
		// TODO: find a more accurate QueryAABB method)
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
				((BumpableBot) robot).onBump(prHead, body.getPosition());
		}
	}

	@Override
	public void draw(Batch batch) {
		if(bounceTimeLeft > 0f)
			bounceSprite.draw(batch);
	}

	@Override
	public void onHeadHit(PlayerRole player) {
		if(player instanceof MarioRole) {
			// only one head hit per update, and only while not bouncing
			if(!isHit && bounceTimeLeft == 0f) {
				isHit = true;
				prHead = player;
				isHitByBig = ((MarioRole) player).isBig();
				runner.enableInteractiveTileUpdates(this);
			}
		}
	}

	protected void setBounceEnabled(boolean e) {
		isBounceEnabled = e;
	}
	protected boolean getBounceEnabled() {
		return isBounceEnabled;
	}

	protected void setBounceImage(TextureRegion textureRegion) {
		bounceSprite.setRegion(textureRegion);
	}

	public boolean isMidBounce() {
		return (bounceTimeLeft > 0f);
	}

	@Override
	public void destroy() {
		if(body != null)
			runner.getWorld().destroyBody(body);
		body = null;
	}

	public abstract void onBump(boolean isBig, PlayerRole prHead);
	public abstract void onBounceStart(boolean isBig, PlayerRole prHead);
	public abstract void onBounceEnd();
}
