package com.ridicarus.kid.tiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.worldrunner.WorldRunner;

public abstract class InteractiveTileObject {
	protected WorldRunner runner;

	protected Rectangle bounds;
	protected Body body;
	protected Fixture fixture;
	protected MapObject object;
	protected boolean isHidden;
	protected int myTileID;

	protected float tileWidth, tileHeight;

	public InteractiveTileObject(WorldRunner runner, MapObject object) {
		this.runner = runner;
		this.object = object;
		this.bounds = ((RectangleMapObject) object).getRectangle();

		isHidden = false;

		BodyDef bdef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		bdef.type = BodyDef.BodyType.StaticBody;
		bdef.position.set(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));
		body = runner.getWorld().createBody(bdef);

		shape.setAsBox(GameInfo.P2M(bounds.getWidth()/2f), GameInfo.P2M(bounds.getHeight()/2f));
		fdef.shape = shape;
		fdef.isSensor = true;
		fixture = body.createFixture(fdef);

		myTileID = getMyTile().getId();

		tileWidth = GameInfo.P2M(bounds.getWidth());
		tileHeight = GameInfo.P2M(bounds.getHeight());
	}

	protected void setCategoryFilter(short filterBit) {
		Filter filter = new Filter();
		filter.categoryBits = filterBit;
		fixture.setFilterData(filter);
	}

	protected void setCategoryAndMaskFilter(short filterBits, short maskBits) {
		Filter filter = new Filter();
		filter.categoryBits = filterBits;
		filter.maskBits = maskBits;
		fixture.setFilterData(filter);
	}

	// the tile related to this object may be created and destroyed elsewhere, so always get the freshest ref
	protected TiledMapTile getMyTile() {
		int x = getTileX();
		int y = getTileY();
		TiledMapTileLayer layer = (TiledMapTileLayer) runner.getMap().getLayers().get(GameInfo.TILEMAP_COLLISION);
		if(layer.getCell(x, y) == null)
			return null;	// return null if cell doesn't exist
		return layer.getCell(x, y).getTile();
	}

	protected void setImageTile(TiledMapTile tile) {
		runner.setImageTile(getTileX(), getTileY(), tile);
	}

	protected void setPhysicTile(boolean solid) {
		runner.setPhysicTile(getTileX(), getTileY(), solid);
	}

	public Vector2 getPosition() {
		return body.getPosition();
	}

	protected int getTileX() {
		return (int) (GameInfo.M2P(body.getPosition().x) / GameInfo.TILEPIX_X);
	}

	protected int getTileY() {
		return (int) (GameInfo.M2P(body.getPosition().y) / GameInfo.TILEPIX_Y);
	}

	public abstract void update(float delta);
	public abstract void draw(Batch batch);
	// head sensor (e.g. for interacting with mario's breakable bricks).
	public abstract void onHeadHit(PlayerRole pr);
	public abstract void destroy();
}
