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
import com.ridicarus.kid.collisionmap.TileCollisionMap;
import com.ridicarus.kid.roles.Player;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.Goomba;
import com.ridicarus.kid.roles.robot.Turtle;
import com.ridicarus.kid.tiles.InteractiveTileObject;
import com.ridicarus.kid.tiles.bumpable.BumpableItemTile;

public class WorldRunner {
	private TextureAtlas atlas;
	private Player player;
	private TiledMap map;
	private TileCollisionMap collisionMap;
	private WorldContactListener contactListener;
	private World world;

	private class PhysicTileForQueue {
		public int x;
		public int y;
		public boolean solid;
		public PhysicTileForQueue(int x, int y, boolean solid) {
			this.x = x;
			this.y = y;
			this.solid = solid;
		}
	}
	private class ImageTileForQueue {
		public int x;
		public int y;
		public TiledMapTile tile;
		public ImageTileForQueue(int x, int y, TiledMapTile tile) {
			this.x = x;
			this.y = y;
			this.tile = tile;
		}
	}

	private LinkedBlockingQueue<PhysicTileForQueue> physicTileChangeQ;
	private LinkedBlockingQueue<ImageTileForQueue> imageTileChangeQ;

	private LinkedList<InteractiveTileObject> intTilesToUpdate;
	private LinkedBlockingQueue<InteractiveTileObject> intTileUpdateEnableQ;
	private LinkedBlockingQueue<InteractiveTileObject> intTileUpdateDisableQ;
	private LinkedBlockingQueue<InteractiveTileObject> intTileDestroyQ;

	private LinkedList<RobotRole> robots;
	private LinkedBlockingQueue<RobotRole> robotAddQ;
	private LinkedBlockingQueue<RobotRole> robotRemoveQ;

	private LinkedList<RobotRole> robotsToUpdate;
	private LinkedBlockingQueue<RobotRole> robotEnableUpdateQ;
	private LinkedBlockingQueue<RobotRole> robotDisableUpdateQ;

//	public enum DrawLayers { BOTTOM, TILE_BACKGROUND, MIDTILE_BACK, TILE_MIDGROUND, MIDTILE_FORE, TILE_FOREGROUND, TOP };
	public enum RobotDrawLayers { NONE, BOTTOM, MIDDLE, TOP };
	private class RobotDrawForQueue {
		public RobotRole robo;
		public RobotDrawLayers drawLayer;
		public RobotDrawForQueue(RobotRole robo, RobotDrawLayers drawLayer) {
			this.robo = robo;
			this.drawLayer = drawLayer;
		}
	}
	private LinkedList<RobotRole>[] robotsToDraw;
	private LinkedBlockingQueue<RobotDrawForQueue> robotSetDrawLayerQ;

	@SuppressWarnings("unchecked")
	public WorldRunner(TextureAtlas atlas) {
		this.atlas = atlas;
		player = null;

		intTilesToUpdate = new LinkedList<InteractiveTileObject>();
		intTileDestroyQ = new LinkedBlockingQueue<InteractiveTileObject>();
		intTileUpdateEnableQ = new LinkedBlockingQueue<InteractiveTileObject>();
		intTileUpdateDisableQ = new LinkedBlockingQueue<InteractiveTileObject>();
		physicTileChangeQ = new LinkedBlockingQueue<PhysicTileForQueue>();
		imageTileChangeQ =  new LinkedBlockingQueue<ImageTileForQueue>();

		robots = new LinkedList<RobotRole>();
		robotAddQ = new LinkedBlockingQueue<RobotRole>();
		robotRemoveQ = new LinkedBlockingQueue<RobotRole>();

		robotsToUpdate = new LinkedList<RobotRole>();
		robotEnableUpdateQ = new LinkedBlockingQueue<RobotRole>();
		robotDisableUpdateQ = new LinkedBlockingQueue<RobotRole>();

		robotsToDraw = (LinkedList<RobotRole>[]) new LinkedList[RobotDrawLayers.values().length];
		for(int i=0; i<RobotDrawLayers.values().length; i++)
			robotsToDraw[i] = new LinkedList<RobotRole>();
		robotSetDrawLayerQ = new LinkedBlockingQueue<RobotDrawForQueue>();

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

		layer = map.getLayers().get(GameInfo.TILEMAP_BUMPABLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			new BumpableItemTile(this, object);

		// load goomba robots layer
		layer = map.getLayers().get(GameInfo.TILEMAP_GOOMBA);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new Goomba(this, object));

		// load turtle robots layer
		layer = map.getLayers().get(GameInfo.TILEMAP_TURTLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new Turtle(this, object));
	}

	public void update(float delta) {
		// update physics world
		world.step(delta, 6, 2);

		player.update(delta);

		updateRobots(delta, player.getRole());

		updateTileWorld(delta);
	}

	public void addRobot(RobotRole robo) {
		robotAddQ.add(robo);
	}

	public void removeRobot(RobotRole robo) {
		robotRemoveQ.add(robo);
	}

	public void enableRobotUpdate(RobotRole robo) {
		robotEnableUpdateQ.add(robo);
	}

	public void disableRobotUpdate(RobotRole robo) {
		robotDisableUpdateQ.add(robo);
	}

	public void setRobotDrawLayer(RobotRole robo, RobotDrawLayers layer) {
		robotSetDrawLayerQ.add(new RobotDrawForQueue(robo, layer));
	}

	private void updateRobots(float delta, PlayerRole player) {
		for(RobotRole robot : robotsToUpdate) {
			// robots are inactive until they are within the player's screen's view (about 14 tiles distance)
			if(robot.getBody().getPosition().x <
					player.getB2Body().getPosition().x + GameInfo.P2M(14 * GameInfo.TILEPIX_X))
				robot.getBody().setActive(true);

			robot.update(delta);
		}

		// during update of robots, some robots may have been added to list of robots to add/remove
		while(!robotAddQ.isEmpty())
			doAddRobot(robotAddQ.poll());
		while(!robotRemoveQ.isEmpty())
			doRemoveRobot(robotRemoveQ.poll());

		while(!robotEnableUpdateQ.isEmpty())
			doEnableRobotUpdate(robotEnableUpdateQ.poll());
		while(!robotDisableUpdateQ.isEmpty())
			doDisableRobotUpdate(robotDisableUpdateQ.poll());

		while(!robotSetDrawLayerQ.isEmpty())
			doSetDrawLayer(robotSetDrawLayerQ.poll());
	}

	private void doAddRobot(RobotRole robo) {
		robots.add(robo);
	}

	private void doRemoveRobot(RobotRole robo) {
		robots.remove(robo);

		if(robotsToUpdate.contains(robo))
			robotsToUpdate.remove(robo);
		for(int i=0; i<RobotDrawLayers.values().length; i++) {
			if(robotsToDraw[i].contains(robo))
				robotsToDraw[i].remove(robo);
		}

		robo.dispose();
	}

	private void doEnableRobotUpdate(RobotRole robo) {
		if(!robotsToUpdate.contains(robo))
			robotsToUpdate.add(robo);
	}

	private void doDisableRobotUpdate(RobotRole robo) {
		if(robotsToUpdate.contains(robo))
			robotsToUpdate.remove(robo);
	}

	private void doSetDrawLayer(RobotDrawForQueue dRobo) {
		// check all layers for robo and remove if necessary
		for(int i=0; i<RobotDrawLayers.values().length; i++) {
			if(robotsToDraw[i].contains(dRobo.robo))
				robotsToDraw[i].remove(dRobo.robo);
		}
		if(dRobo.drawLayer != RobotDrawLayers.NONE)
			robotsToDraw[dRobo.drawLayer.ordinal()].add(dRobo.robo);
	}

	public void setPhysicTile(int x, int y, boolean solid) {
		physicTileChangeQ.add(new PhysicTileForQueue(x, y, solid));
	}

	public void setImageTile(int x, int y, TiledMapTile tile) {
		imageTileChangeQ.add(new ImageTileForQueue(x, y, tile));
	}

	// tile creates and destroys
	private void updateTileWorld(float delta) {
		for(InteractiveTileObject tile : intTilesToUpdate)
			tile.update(delta);

		// updating interactive tiles might add tiles to the hide or unhide queues
		while(!intTileUpdateEnableQ.isEmpty()) {
			InteractiveTileObject tile = intTileUpdateEnableQ.poll();
			intTilesToUpdate.add(tile);
		}
		while(!intTileUpdateDisableQ.isEmpty()) {
			InteractiveTileObject tile = intTileUpdateDisableQ.poll();
			intTilesToUpdate.remove(tile);
		}

		while(!intTileDestroyQ.isEmpty()) {
			InteractiveTileObject tile = intTileDestroyQ.poll();

			// remove tile from updates list if it's in there
			if(intTilesToUpdate.contains(tile))
				intTilesToUpdate.remove(tile);

			tile.destroy();
		}

		while(!physicTileChangeQ.isEmpty()) {
			PhysicTileForQueue pTile = physicTileChangeQ.poll();
			if(pTile.solid == true)
				collisionMap.addTile(pTile.x, pTile.y);
			else
				collisionMap.removeTile(pTile.x, pTile.y);
		}
		while(!imageTileChangeQ.isEmpty()) {
			ImageTileForQueue iTile = imageTileChangeQ.poll();

			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(GameInfo.TILEMAP_COLLISION);
			// create a cell for the tile if necessary
			if(layer.getCell(iTile.x, iTile.y) == null)
				layer.setCell(iTile.x, iTile.y, new Cell());

			// create the tile on the graphics side
			layer.getCell(iTile.x, iTile.y).setTile(iTile.tile);
		}
	}

	public void enableInteractiveTileUpdates(InteractiveTileObject tile) {
		// ignore tiles that already receive updates
		if(intTilesToUpdate.contains(tile))
			return;

		intTileUpdateEnableQ.add(tile);
	}

	public void disableInteractiveTileUpdates(InteractiveTileObject tile) {
		if(!intTilesToUpdate.contains(tile))
			return;

		intTileUpdateDisableQ.add(tile);
	}

	public void destroyInteractiveTile(InteractiveTileObject tile) {
		intTileDestroyQ.add(tile);
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

//	public LinkedList<RobotRole> getRobots() {
//		return robots;
//	}
	public LinkedList<RobotRole>[] getRobotsToDraw() {
		return robotsToDraw;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public void dispose() {
		collisionMap.dispose();
		world.dispose();
	}

	public LinkedList<InteractiveTileObject> getIntTilesToUpdate() {
		return intTilesToUpdate;
	}

	public Player getPlayer() {
		return player;
	}
}
