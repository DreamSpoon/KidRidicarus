package com.ridicarus.kid.tools;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.SpecialTiles.InteractiveTileObject;
import com.ridicarus.kid.SpecialTiles.BumpableTile;
import com.ridicarus.kid.collisionmap.TileCollisionMap;
import com.ridicarus.kid.roles.Player;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.Goomba;
import com.ridicarus.kid.roles.robot.Turtle;

public class WorldRunner {
	private TextureAtlas atlas;
	private Player player;
	private TiledMap map;
	private TileCollisionMap collisionMap;
	private WorldContactListener contactListener;
	private World world;

	private class TileForQueue {
		public int x;
		public int y;
		public TiledMapTile tile;
		public TileForQueue(int x, int y, TiledMapTile tile) {
			this.x = x;
			this.y = y;
			this.tile = tile;
		}
	}
    private LinkedBlockingQueue<TileForQueue> tilesToCreate;
    private LinkedBlockingQueue<TileForQueue> tilesToDestroy;
	private LinkedBlockingQueue<TileForQueue> tilesToHide;
	private LinkedBlockingQueue<TileForQueue> tilesToUnhide;
	private LinkedBlockingQueue<TileForQueue> tilesToChange;

	private LinkedList<InteractiveTileObject> intTilesToUpdate;
	private LinkedBlockingQueue<InteractiveTileObject> intTilesToAddToUpdate;
	private LinkedBlockingQueue<InteractiveTileObject> intTilesToRemoveFromUpdate;
	private LinkedBlockingQueue<InteractiveTileObject> intTilesToDestroy;

	private LinkedList<RobotRole> robots;
	private LinkedBlockingQueue<RobotRole> robotsToAdd;
	private LinkedBlockingQueue<RobotRole> robotsToRemove;

	public WorldRunner(TextureAtlas atlas) {
		this.atlas = atlas;
		player = null;

		tilesToDestroy = new LinkedBlockingQueue<TileForQueue>();
		tilesToCreate = new LinkedBlockingQueue<TileForQueue>();
		intTilesToUpdate = new LinkedList<InteractiveTileObject>();
		intTilesToDestroy = new LinkedBlockingQueue<InteractiveTileObject>();
		intTilesToAddToUpdate = new LinkedBlockingQueue<InteractiveTileObject>();
		intTilesToRemoveFromUpdate = new LinkedBlockingQueue<InteractiveTileObject>();
		tilesToHide = new LinkedBlockingQueue<TileForQueue>();
		tilesToUnhide = new LinkedBlockingQueue<TileForQueue>();
		tilesToChange = new LinkedBlockingQueue<TileForQueue>();

		robots = new LinkedList<RobotRole>();
		robotsToAdd = new LinkedBlockingQueue<RobotRole>();
		robotsToRemove = new LinkedBlockingQueue<RobotRole>();

		world = new World(new Vector2(0, -10f), true);
		contactListener = new WorldContactListener();
		world.setContactListener(contactListener);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void loadMap(TiledMap map) {
		MapLayer layer;
		int width, height, tileWidth, tileHeight;

		this.map = map;

		// DEBUG: check to see if the get method returns null.
		width = (Integer) map.getProperties().get("width");
		height = (Integer) map.getProperties().get("height");
		tileWidth = (Integer) map.getProperties().get("tilewidth");
		tileHeight = (Integer) map.getProperties().get("tileheight");

		// create collision map from a certain tiled layer
		collisionMap = new TileCollisionMap(world,
				(TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION),
				width, height, tileWidth, tileHeight);

		layer = map.getLayers().get(GameInfo.TILEMAP_BRICK);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
//			new BrickTile(this, object);
			new BumpableTile(this, object);
		}
		layer = map.getLayers().get(GameInfo.TILEMAP_COINBLOCK);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
//			new CoinTile(this, object);
			new BumpableTile(this, object);
		}

		// load goomba robots layer
		layer = map.getLayers().get(GameInfo.TILEMAP_GOOMBA);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
			robots.add(new Goomba(this, object));
		}
		layer = map.getLayers().get(GameInfo.TILEMAP_TURTLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
			robots.add(new Turtle(this, object));
		}
	}

	public void update(float delta) {
		// update physics world
		world.step(delta, 6, 2);

		player.update(delta);

		updateRobots(delta, player.getRole());

		updateTileWorld(delta);
	}

	public void addRobot(RobotRole robo) {
		robotsToAdd.add(robo);
	}

	public void removeRobot(RobotRole robo) {
		robotsToRemove.add(robo);
	}

	private void updateRobots(float delta, PlayerRole player) {
		for(RobotRole robot : robots) {
			// robots are inactive until they are within the player's screen's view (about 14 tiles distance)
			if(robot.getBody().getPosition().x <
					player.getB2Body().getPosition().x + GameInfo.P2M(14 * GameInfo.TILEPIX_X))
				robot.getBody().setActive(true);

			robot.update(delta);
		}

		// during update of robots, some robots may have been added to list of robots to add/remove
		while(!robotsToAdd.isEmpty()) {
			RobotRole robo = robotsToAdd.poll();
			robots.add(robo);
		}
		while(!robotsToRemove.isEmpty()) {
			RobotRole robo = robotsToRemove.poll();
			robo.dispose();
			robots.remove(robo);
		}
	}

	public void destroyTile(int x, int y) {
		tilesToDestroy.add(new TileForQueue(x, y, null));
	}

	public void createTile(int x, int y, TiledMapTile tile) {
		tilesToCreate.add(new TileForQueue(x, y, tile));
	}

	public void hideTile(int x, int y) {
		tilesToHide.add(new TileForQueue(x, y, null));
	}

	public void unhideTile(int x, int y, TiledMapTile tile) {
		tilesToUnhide.add(new TileForQueue(x, y, tile));
	}

	public void changeTile(int x, int y, TiledMapTile tile) {
		tilesToChange.add(new TileForQueue(x, y, tile));
	}

	// tile creates and destroys
	private void updateTileWorld(float delta) {
		for(InteractiveTileObject tile : intTilesToUpdate)
			tile.update(delta);

		while(!tilesToDestroy.isEmpty()) {
			TileForQueue ttd = tilesToDestroy.poll();
			reallyDestroyTile(ttd.x, ttd.y);
		}
		while(!tilesToCreate.isEmpty()) {
			TileForQueue ttd = tilesToCreate.poll();
			reallyCreateTile(ttd.x, ttd.y, ttd.tile);
		}

		// updating interactive tiles might add tiles to the hide or unhide queues
		while(!intTilesToAddToUpdate.isEmpty()) {
			InteractiveTileObject tile = intTilesToAddToUpdate.poll();
			intTilesToUpdate.add(tile);
		}
		while(!intTilesToRemoveFromUpdate.isEmpty()) {
			InteractiveTileObject tile = intTilesToRemoveFromUpdate.poll();
			intTilesToUpdate.remove(tile);
		}

		while(!intTilesToDestroy.isEmpty()) {
			InteractiveTileObject tile = intTilesToDestroy.poll();

			// remove tile from updates list if it's in there
			if(intTilesToUpdate.contains(tile))
				intTilesToUpdate.remove(tile);

			tile.destroy();
		}

		// check queues after interactive tile updates finish
		// TODO: verify that the order of the below 3 blocks is correct
		while(!tilesToHide.isEmpty()) {
			TileForQueue ttd = tilesToHide.poll();
			reallyHideTile(ttd.x, ttd.y);
		}
		while(!tilesToUnhide.isEmpty()) {
			TileForQueue ttd = tilesToUnhide.poll();
			reallyUnhideTile(ttd.x, ttd.y, ttd.tile);
		}
		while(!tilesToChange.isEmpty()) {
			TileForQueue ttd = tilesToChange.poll();
			reallyChangeTile(ttd.x, ttd.y, ttd.tile);
		}
	}

	private void reallyCreateTile(int x, int y, TiledMapTile tile) {
		TiledMapTileLayer layer;

		if(tile == null)
			throw new IllegalArgumentException("Cannot create tile from null! Argument 'tile' must not equal null.");

		// check the graphics tile map to see if the tile has been destroyed
		layer = (TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION);
		if(layer.getCell(x, y) != null) {
			if(layer.getCell(x, y).getTile() != null)
				return;	// exit if the tile already exists
		}
		else	// create the cell because we need to set it's tile 
			layer.setCell(x, y, new Cell());

		// create the tile on the graphics side
		layer.getCell(x, y).setTile(tile);

		// create the tile on the collision side
		collisionMap.toggleTile(x, y);
	}

	private void reallyDestroyTile(int x, int y) {
		TiledMapTileLayer layer;

		// check the graphics tile map to see if the tile has been destroyed
		layer = (TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION);
		if(layer.getCell(x,  y) == null)
			return;	// exit if cell doesn't exist
		if(layer.getCell(x, y).getTile() == null)
			return;	// exit if tile doesn't exist

		// destroy the tile on the graphics side
		layer.getCell(x, y).setTile(null);

		// destroy the tile on the collision side
		collisionMap.toggleTile(x, y);
	}

	private void reallyHideTile(int x, int y) {
		TiledMapTileLayer layer;

		// check the graphics tile map to see if the tile exists
		layer = (TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION);
		if(layer.getCell(x,  y) == null)
			return;	// exit if cell doesn't exist
		if(layer.getCell(x, y).getTile() == null)
			return;	// exit if tile doesn't exist

		// destroy the tile on the graphics side
		layer.getCell(x, y).setTile(null);

		// do not destroy the tile on the collision side
	}

	private void reallyUnhideTile(int x, int y, TiledMapTile tile) {
		TiledMapTileLayer layer;

		if(tile == null)
			throw new IllegalArgumentException("Cannot unhide tile from null! Argument 'tile' must not equal null.");

		// check the graphics tile map to see if the tile exists already
		layer = (TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION);
		if(layer.getCell(x, y) != null) {
			if(layer.getCell(x, y).getTile() != null)
				return;	// exit if the tile already exists
		}
		else	// create the cell because we need to set it's tile 
			layer.setCell(x, y, new Cell());

		// create the tile on the graphics side
		layer.getCell(x, y).setTile(tile);

		// do not recreate the tile on the collision side, since hiding/unhiding does not destroy the tile
	}

	// assuming tile is hidden
	private void reallyChangeTile(int x, int y, TiledMapTile tile) {
		TiledMapTileLayer layer;
		boolean isNew = false;

		if(tile == null)
			throw new IllegalArgumentException("Argument 'tile' must not equal null.");

		layer = (TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION);

		// create the cell if necessary
		if(layer.getCell(x, y) == null) {
			layer.setCell(x, y, new Cell());
			isNew = true;
		}
		else if (layer.getCell(x, y).getTile() == null)
			isNew = true;

		if(!isNew)
			throw new IllegalStateException("Cannot change tile that was not already hidden.");

		// create the tile on the graphics side
		layer.getCell(x, y).setTile(tile);
	}

	public void enableInteractiveTileUpdates(InteractiveTileObject tile) {
		if(intTilesToUpdate.contains(tile))
			return;

		intTilesToAddToUpdate.add(tile);
	}

	public void disableInteractiveTileUpdates(InteractiveTileObject tile) {
		if(!intTilesToUpdate.contains(tile))
			return;

		intTilesToRemoveFromUpdate.add(tile);
	}

	public void destroyInteractiveTile(InteractiveTileObject tile) {
		intTilesToDestroy.add(tile);
	}

	public World getWorld() {
		return world;
	}

	public TiledMap getMap() {
		return map;
	}

	public TileCollisionMap getCollisionMap() {
		return collisionMap;
	}

	public LinkedList<RobotRole> getRobots() {
		return robots;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public void dispose() {
		world.dispose();
	}

	public LinkedList<InteractiveTileObject> getIntTilesToUpdate() {
		return intTilesToUpdate;
	}

	public Player getPlayer() {
		return player;
	}
}
