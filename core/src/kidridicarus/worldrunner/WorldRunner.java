package kidridicarus.worldrunner;

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

import kidridicarus.GameInfo;
import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.InfoSMB.PointAmount;
import kidridicarus.collisionmap.TileCollisionMap;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.SMB.CastleFlag;
import kidridicarus.roles.robot.SMB.Flagpole;
import kidridicarus.roles.robot.SMB.FloatingPoints;
import kidridicarus.roles.robot.SMB.LevelEnd;
import kidridicarus.roles.robot.SMB.PipeWarp;
import kidridicarus.roles.robot.SMB.enemy.GoombaRole;
import kidridicarus.roles.robot.SMB.enemy.TurtleRole;
import kidridicarus.roles.robot.SMB.item.StaticCoin;
import kidridicarus.tiles.InteractiveTileObject;
import kidridicarus.tiles.SMB.BumpableItemTile;

/*
 * TODO:
 * -create an encapsulating class for the Queues, e.g.
 *	class EncapQ<T> {
 *		public void queueAdd(T item) {
 *			...
 *		}
 *		public void queueRemove(T item) {
 *			...
 *		}
 *		public void processQueues(T item) {
 *			for each item to add, call processAdd method supplied to constructor
 *			for each item to remove, call processRemove method supplied to constructor
 *		}
 *	}
 */
public class WorldRunner {
	private static final float LEVEL_MAX_TIME = 300f;

	private TextureAtlas atlas;
	private Player player;
	private TiledMap map;
	private TileCollisionMap collisionMap;
	private WorldContactListener contactListener;
	private World world;
	private AssetManager manager;
	private String currentRoomMusicName;
	private Music currentRoomMusic;
	private LinkedList<Room> rooms;
	private float levelTimeRemaining;

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

	private CastleFlag castleFlag;

	@SuppressWarnings("unchecked")
	public WorldRunner(AssetManager manager, TextureAtlas atlas, OrthographicCamera gamecam) {
		this.manager = manager;
		this.atlas = atlas;
		this.gamecam = gamecam;

		player = null;
		levelTimeRemaining = LEVEL_MAX_TIME;

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

		currentRoomMusicName = "";
		currentRoomMusic = null;

		spawnpoints = new LinkedList<Spawnpoint>();

		castleFlag = null;
	}

	public void loadMap(TiledMap map, AssetManager manager) {
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

		// load the bumpable tiles (e.g. breakable bricks)
		layer = map.getLayers().get(GameInfo.TILEMAP_BUMPABLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			new BumpableItemTile(this, object);

		// load goomba layer
		layer = map.getLayers().get(GameInfo.TILEMAP_GOOMBA);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new GoombaRole(this, object));

		// load turtle layer
		layer = map.getLayers().get(GameInfo.TILEMAP_TURTLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new TurtleRole(this, object));

		// load coin layer
		layer = map.getLayers().get(GameInfo.TILEMAP_STATICCOIN);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new StaticCoin(this, object));

		// load flagpole (should be only one)
		layer = map.getLayers().get(GameInfo.TILEMAP_FLAGPOLE);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new Flagpole(this, object));

		// load level end (should be only one)
		layer = map.getLayers().get(GameInfo.TILEMAP_LEVELEND);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new LevelEnd(this, object));

		// load spawnpoint
		layer = map.getLayers().get(GameInfo.TILEMAP_SPAWNPOINT);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			spawnpoints.add(new Spawnpoint(object));

		// load pipe entrance(s) after loading spawnpoints, since pipe entrances need to get refs to spawnpoints
		layer = map.getLayers().get(GameInfo.TILEMAP_PIPEWARP);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			robots.add(new PipeWarp(this, object, getSpawnpointFromObject(object)));

		layer = map.getLayers().get(GameInfo.TILEMAP_ROOMS);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			rooms.add(new Room(this, object));

		layer = map.getLayers().get(GameInfo.TILEMAP_DESPAWN);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			new DespawnBox(this, object);

		// Note: there should be only one castle flag
		layer = map.getLayers().get(GameInfo.TILEMAP_CASTLEFLAG);
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class))
			castleFlag = new CastleFlag(this, object);

		preloadRoomMusic();
	}

	private void preloadRoomMusic() {
		LinkedList<String> musicCatalog = new LinkedList<String>();
		for(Room r : rooms) {
			if(!musicCatalog.contains(r.getRoommusic()))
				musicCatalog.add(r.getRoommusic());
		}

		for(String m : musicCatalog)
			manager.load(m, Music.class);
		manager.finishLoading();
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

		if(player.getRole() instanceof MarioRole && player.getRole().isOnGround()) {
			((MarioRole) player.getRole()).resetFlyingPoints();
		}
		player.update(delta);
		// if player is not dead then use their current room to determine the gamecam position
		if(!player.getRole().isDead()) {
			Room inRoom = getPlayerRoom();
			if(inRoom != null) {
				// set view cam position
				inRoom.setGamecamPosition(gamecam, player.getRole());

				// check for music change
				if(currentRoomMusic == null || !inRoom.getRoommusic().equals(currentRoomMusicName))
					changeRoomMusic(inRoom.getRoommusic());
			}
		}

		updateRobots(delta, player.getRole());

		updateTileWorld(delta);

		levelTimeRemaining -= delta;
	}

	// get a room!
	private Room getPlayerRoom() {
		Room foundRoom = null;
		// TODO: what if player's bounds rectangle is in two or more rooms simultaneously?
		for(Room r : rooms) {
			if(r.isPlayerInRoom(player.getRole())) {
				foundRoom = r;
				break;
			}
		}
		return foundRoom;
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
			if(robot instanceof GoombaRole || robot instanceof TurtleRole) {
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
		player.dispose();
		collisionMap.dispose();
		world.dispose();
	}

	public LinkedList<InteractiveTileObject> getIntTilesToUpdate() {
		return intTilesToUpdate;
	}

	public void playSound(String sound) {
		manager.get(sound, Sound.class).play(GameInfo.SOUND_VOLUME);
	}

	private void changeRoomMusic(String musicname) {
		if(currentRoomMusic != null)
			currentRoomMusic.stop();

		currentRoomMusic = manager.get(musicname, Music.class);
		currentRoomMusic.setLooping(true);
		currentRoomMusic.setVolume(GameInfo.MUSIC_VOLUME);
		currentRoomMusic.play();

		currentRoomMusicName = musicname;
	}

	public void startRoomMusic() {
		if(currentRoomMusic != null) {
			currentRoomMusic = manager.get(currentRoomMusicName, Music.class);
			currentRoomMusic.setLooping(true);
			currentRoomMusic.setVolume(GameInfo.MUSIC_VOLUME);
			currentRoomMusic.play();
		}
	}

	public void stopRoomMusic() {
		if(currentRoomMusic != null)
			currentRoomMusic.stop();
	}

	// play music, no loop (for things like mario powerstar)
	public void startSinglePlayMusic(String musicName) {
		Music otherMusic = manager.get(musicName, Music.class);
		otherMusic.setLooping(false);
		otherMusic.setVolume(GameInfo.MUSIC_VOLUME);
		otherMusic.play();
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

	// add points to mario's total, with option to display the point amount on-screen
	public void givePlayerPoints(PlayerRole role, PointAmount amount, boolean visible, Vector2 position,
			float yOffset, boolean isHeadBounce) {
		MarioRole mario;
		PointAmount finalAmt;

		if(role == null)
			throw new IllegalArgumentException("Cannot give points to null player.");

		finalAmt = amount;
		if(role instanceof MarioRole) {
			mario = (MarioRole) role;
			// check for points increase due to mario bouncing multiple times without touching the ground
			if(isHeadBounce) {
				mario.incrementFlyingPoints();
				// if the flying points are greater than the incoming points amount then use the flying points
				if(mario.getFlyingPoints().compareTo(amount) > 0 || mario.getFlyingPoints() == PointAmount.UP1)
					finalAmt = mario.getFlyingPoints();
			}

			if(finalAmt == PointAmount.UP1) {
				playSound(GameInfo.SOUND_1UP);
				mario.give1UP();
			}
			else
				mario.givePoints(finalAmt);
		}
		// if visible then create floating points that despawn after a short time
		if(visible && position != null)
			addRobot(new FloatingPoints(this, finalAmt, position.cpy().add(0f, yOffset)));
	}

	public float getLevelTimeRemaining() {
		return levelTimeRemaining;
	}

	public void triggerCastleFlag() {
		if(castleFlag != null)
			castleFlag.trigger();
	}

	public Vector2 posToMapTileOffset(Vector2 position) {
		return new Vector2((int) (GameInfo.M2P(position.x) / GameInfo.TILEPIX_X),
				(int) (GameInfo.M2P(position.y) / GameInfo.TILEPIX_Y));
	}

	/*
	 * Returns the sub-position within the tile for each axis, as a ratio of the tile size on the axis.
	 * e.g. if TILEPIX_X = 16 and positionX = 19, then returnsX 3/16=0.1875
	 * e.g. if TILEPIX_Y = 16 and positionY = 42, then returnsY 10/16=0.625
	 */
	public Vector2 posToMapTileSubOffset(Vector2 position) {
		float tileW = GameInfo.P2M(GameInfo.TILEPIX_X);
		float tileH = GameInfo.P2M(GameInfo.TILEPIX_Y);
		// mod position coordinate with tile size
		float x = (int) (position.x / tileW);
		float y = (int) (position.y / tileH);
		// subtract modded position and divide by tile size
		return new Vector2((position.x - x * tileW) / tileW, (position.y - y * tileH) / tileH);
	}

	public boolean isMapTileSolid(Vector2 tilePos) {
		return collisionMap.isTileExist((int) tilePos.x, (int) tilePos.y);
	}
}
