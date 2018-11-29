package com.ridicarus.kid.tools;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.collisionmap.TileCollisionMap;
import com.ridicarus.kid.roles.Player;
import com.ridicarus.kid.roles.PlayerRole;
import com.ridicarus.kid.roles.RobotRole;
import com.ridicarus.kid.roles.robot.Flagpole;
import com.ridicarus.kid.roles.robot.Goomba;
import com.ridicarus.kid.roles.robot.Levelend;
import com.ridicarus.kid.roles.robot.PipeEntrance;
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
	private AssetManager manager;
	private Music currentMusic;
	private LinkedList<Room> rooms;

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

	// list of all robots
	private LinkedList<RobotRole> robots;
	private LinkedBlockingQueue<RobotRole> robotAddQ;
	private LinkedBlockingQueue<RobotRole> robotRemoveQ;

	// sub-list of robots, just robots receiving updates
	private LinkedList<RobotRole> robotsToUpdate;
	private LinkedBlockingQueue<RobotRole> robotEnableUpdateQ;
	private LinkedBlockingQueue<RobotRole> robotDisableUpdateQ;

	private class RobotDrawForQueue {
		public RobotRole robo;
		public SpriteDrawOrder drawOrder;
		public RobotDrawForQueue(RobotRole robo, SpriteDrawOrder drawOrder) {
			this.robo = robo;
			this.drawOrder = drawOrder;
		}
	}
	// sub-list of robots, just robots being drawn
	private LinkedList<RobotRole>[] robotsToDraw;
	private LinkedBlockingQueue<RobotDrawForQueue> robotSetDrawLayerQ;

	private LinkedList<Spawnpoint> spawnpoints;

	private OrthographicCamera gamecam;

	@SuppressWarnings("unchecked")
	public WorldRunner(AssetManager manager, TextureAtlas atlas, OrthographicCamera gamecam) {
		this.manager = manager;
		this.atlas = atlas;
		this.gamecam = gamecam;

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

		robotsToDraw = (LinkedList<RobotRole>[]) new LinkedList[SpriteDrawOrder.values().length];
		for(int i=0; i<SpriteDrawOrder.values().length; i++)
			robotsToDraw[i] = new LinkedList<RobotRole>();
		robotSetDrawLayerQ = new LinkedBlockingQueue<RobotDrawForQueue>();

		rooms = new LinkedList<Room>();

		world = new World(new Vector2(0, -10f), true);
		contactListener = new WorldContactListener();
		world.setContactListener(contactListener);

		currentMusic = null;
		startMusic(GameInfo.MUSIC_MARIO, true);

		spawnpoints = new LinkedList<Spawnpoint>();
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

		// load flagpole (should be only one)
		layer = map.getLayers().get(GameInfo.TILEMAP_FLAGPOLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new Flagpole(this, object));

		// load level end (should be only one)
		layer = map.getLayers().get(GameInfo.TILEMAP_LEVELEND);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new Levelend(this, object));

		// load spawnpoint
		layer = map.getLayers().get(GameInfo.TILEMAP_SPAWNPOINT);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			spawnpoints.add(new Spawnpoint(object));

		// load pipe entrance(s) after loading spawnpoints, since pipe entrances need to get refs to spawnpoints
		layer = map.getLayers().get(GameInfo.TILEMAP_PIPEWARP);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new PipeEntrance(this, object, getSpawnpointFromObject(object)));

		layer = map.getLayers().get(GameInfo.TILEMAP_ROOMS);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			rooms.add(new Room(this, object));
	}

	// Note: Load spawnpoints before using this method.
	private Spawnpoint getSpawnpointFromObject(MapObject object) {
		if(!object.getProperties().containsKey(GameInfo.OBJKEY_EXITNAME))
			return null;

		Spawnpoint spReturn = null;
		String spName = object.getProperties().get(GameInfo.OBJKEY_EXITNAME, String.class);
		for(Spawnpoint sp : spawnpoints) {
			if(sp.getName().equals(spName)) {
				spReturn = sp;
				break;
			}
		}

		return spReturn;
	}

	public void update(float delta) {
		// If player needs to warp, do it after the update finishes() and the draw() finishes, then apply warp
		// just before the start of the next update() call, which would be right here.
		Spawnpoint sp = player.getRole().getWarpSpawnpoint(); 
		if(sp != null)
			player.getRole().respawn(sp);

		// update physics world
		world.step(delta, 6, 2);

		player.update(delta);
		// if player is not dead then use their current room to determine the gamecam position
		if(!player.getRole().isDead()) {
			Room inRoom = null;
			// TODO: what if player's bounds rectangle is in two or more rooms simultaneously?
			for(Room r : rooms) {
				if(r.isPlayerInRoom(player.getRole())) {
					inRoom = r;
					break;
				}
			}
			if(inRoom != null)
				inRoom.setGamecamPosition(gamecam, player.getRole());
		}

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

	public void setRobotDrawLayer(RobotRole robo, SpriteDrawOrder layer) {
		robotSetDrawLayerQ.add(new RobotDrawForQueue(robo, layer));
	}

	private void updateRobots(float delta, PlayerRole player) {
		for(RobotRole robot : robotsToUpdate) {
			if(robot instanceof Goomba || robot instanceof Turtle) {
				// robots are inactive until they are within the player's screen's view (about 14 tiles distance)
				if(robot.getPosition().x < player.getPosition().x + GameInfo.P2M(14 * GameInfo.TILEPIX_X))
					robot.setActive(true);
			}

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
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
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
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
			if(robotsToDraw[i].contains(dRobo.robo))
				robotsToDraw[i].remove(dRobo.robo);
		}
		if(dRobo.drawOrder != SpriteDrawOrder.NONE)
			robotsToDraw[dRobo.drawOrder.ordinal()].add(dRobo.robo);
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

	// WorldRenderer will get and render the robots.
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

	public void playSound(String sound) {
		manager.get(sound, Sound.class).play(GameInfo.SOUND_VOLUME);
	}

	public void startMusic(String musicName, boolean looping) {
		stopMusic();

		currentMusic = manager.get(musicName, Music.class);
		currentMusic.setLooping(looping);
		currentMusic.setVolume(GameInfo.MUSIC_VOLUME);
		currentMusic.play();
	}

	public void stopMusic() {
		if(currentMusic != null) {
			currentMusic.stop();
			currentMusic = null;
		}
	}

	public Player createPlayer() {
		if(player != null)
			throw new IllegalStateException("Player already created. Cannot create again.");

		// find the level's main spawnpoint and spawn the player there
		boolean spawned = false;
		for(Spawnpoint spawn : spawnpoints) {
			if(spawn.isMainSpawn()) {
				spawned = true;
				player = new Player(this, spawn.getCenter());
				break;
			}
		}
		// if no main spawnpoint found then spawn player at (0, 0)
		if(!spawned)
			player = new Player(this, new Vector2(0f, 0f));
		return player;
	}

	public Player getPlayer() {
		return player;
	}
}
