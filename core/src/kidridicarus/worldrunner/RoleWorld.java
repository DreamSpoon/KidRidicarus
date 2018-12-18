package kidridicarus.worldrunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.collisionmap.TileCollisionMap;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.SMB.BrickPiece;
import kidridicarus.roles.robot.SMB.BumpTile;
import kidridicarus.roles.robot.SMB.CastleFlag;
import kidridicarus.roles.robot.SMB.Flagpole;
import kidridicarus.roles.robot.SMB.LevelEndTrigger;
import kidridicarus.roles.robot.SMB.MarioFireball;
import kidridicarus.roles.robot.SMB.PipeWarp;
import kidridicarus.roles.robot.SMB.SpinCoin;
import kidridicarus.roles.robot.SMB.enemy.GoombaRole;
import kidridicarus.roles.robot.SMB.enemy.TurtleRole;
import kidridicarus.roles.robot.SMB.item.FireFlower;
import kidridicarus.roles.robot.SMB.item.Mush1UP;
import kidridicarus.roles.robot.SMB.item.PowerMushroom;
import kidridicarus.roles.robot.SMB.item.StaticCoin;
import kidridicarus.roles.robot.general.DespawnBox;
import kidridicarus.roles.robot.general.PlayerSpawner;
import kidridicarus.roles.robot.general.RobotSpawnBox;
import kidridicarus.roles.robot.general.RobotSpawnTrigger;
import kidridicarus.roles.robot.general.Room;
import kidridicarus.tools.BasicInputs;
import kidridicarus.tools.EncapTexAtlas;

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
public class RoleWorld implements Disposable {
	private static final float LEVEL_MAX_TIME = 300f;

	private World world;
	private WorldContactListener contactListener;
	private TileCollisionMap collisionMap;

	// list of all robots
	private LinkedList<RobotRole> robots;
	private LinkedBlockingQueue<RobotRole> robotAddQ;
	private LinkedBlockingQueue<RobotRole> robotDestroyQ;

	// sub-list of robots, just robots receiving updates
	private LinkedList<RobotRole> updateRobots;
	private LinkedBlockingQueue<RobotRole> updateRobotEnableQ;
	private LinkedBlockingQueue<RobotRole> updateRobotDisableQ;

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
	private LinkedBlockingQueue<PhysicTileForQueue> physicTileChangeQ;

	private class RobotDrawForQueue {
		public RobotRole robo;
		public SpriteDrawOrder drawOrder;
		public RobotDrawForQueue(RobotRole robo, SpriteDrawOrder drawOrder) {
			this.robo = robo;
			this.drawOrder = drawOrder;
		}
	}
	// sub-list of robots, just robots being drawn
	private LinkedList<RobotRole>[] drawRobots;
	private LinkedBlockingQueue<RobotDrawForQueue> setRobotDrawLayerQ;

	private EncapTexAtlas encapTexAtlas;
	private RoleEventListener roleEventListener;

	private float levelTimeRemaining;

	private MarioRole playerRole;

	@SuppressWarnings("unchecked")
	public RoleWorld() {
		encapTexAtlas = null;

		levelTimeRemaining = LEVEL_MAX_TIME;

		physicTileChangeQ = new LinkedBlockingQueue<PhysicTileForQueue>();

		robots = new LinkedList<RobotRole>();
		robotAddQ = new LinkedBlockingQueue<RobotRole>();
		robotDestroyQ = new LinkedBlockingQueue<RobotRole>();

		updateRobots = new LinkedList<RobotRole>();
		updateRobotEnableQ = new LinkedBlockingQueue<RobotRole>();
		updateRobotDisableQ = new LinkedBlockingQueue<RobotRole>();

		drawRobots = (LinkedList<RobotRole>[]) new LinkedList[SpriteDrawOrder.values().length];
		for(int i=0; i<SpriteDrawOrder.values().length; i++)
			drawRobots[i] = new LinkedList<RobotRole>();
		setRobotDrawLayerQ = new LinkedBlockingQueue<RobotDrawForQueue>();

		world = new World(new Vector2(0, -10f), true);
		contactListener = new WorldContactListener();
		world.setContactListener(contactListener);
	}

	public void setEncapTexAtlas(EncapTexAtlas encapTexAtlas) {
		this.encapTexAtlas = encapTexAtlas;
	}

	// uses the input layers to find solid (i.e. non-null) tiles and create the collision map
	public void createCollisionMap(Collection<TiledMapTileLayer> solidLayers) {
		collisionMap = new TileCollisionMap(world, solidLayers);
	}

	// 
	public void step(float delta, BasicInputs bi) {
		world.step(delta, 6, 2);

		playerRole.update(delta, bi);
		updateRobots(delta);
		updateTileWorld(delta);

		levelTimeRemaining -= delta;
	}

	public void destroyRobot(RobotRole robo) {
		robotDestroyQ.add(robo);
	}


	public void createRobots(Collection<RobotRoleDef> robotDefs) {
		Iterator<RobotRoleDef> rrIter = robotDefs.iterator();
		while(rrIter.hasNext()) {
			RobotRoleDef rrDef = rrIter.next();
			createRobot(rrDef);
		}
	}

	public RobotRole createRobot(RobotRoleDef rdef) {
		String rClass = rdef.properties.get(KVInfo.KEY_ROBOTROLECLASS, String.class);

		RobotRole rr = null;
		if(rClass.equals(KVInfo.VAL_BUMPTILE))
			doAddRobot(rr = new BumpTile(this, rdef));
		else if(rClass.equals(KVInfo.VAL_GOOMBA))
			doAddRobot(rr = new GoombaRole(this, rdef));
		else if(rClass.equals(KVInfo.VAL_TURTLE))
			doAddRobot(rr = new TurtleRole(this, rdef));
		else if(rClass.equals(KVInfo.VAL_COIN))
			doAddRobot(rr = new StaticCoin(this, rdef));
		else if(rClass.equals(KVInfo.VAL_SPAWNGOOMBA) || rClass.equals(KVInfo.VAL_SPAWNTURTLE))
			doAddRobot(rr = new RobotSpawnBox(this, rdef));
		else if(rClass.equals(KVInfo.VAL_SPAWNPLAYER))
			doAddRobot(rr = new PlayerSpawner(this, rdef));
		else if(rClass.equals(KVInfo.VAL_ROOM))
			doAddRobot(rr = new Room(this, rdef));
		else if(rClass.equals(KVInfo.VAL_FLAGPOLE))
			doAddRobot(rr = new Flagpole(this, rdef));
		else if(rClass.equals(KVInfo.VAL_LEVELEND_TRIGGER))
			doAddRobot(rr = new LevelEndTrigger(this, rdef));
		else if(rClass.equals(KVInfo.VAL_PIPEWARP))
			doAddRobot(rr = new PipeWarp(this, rdef));
		else if(rClass.equals(KVInfo.VAL_DESPAWN))
			doAddRobot(rr = new DespawnBox(this, rdef));
		else if(rClass.equals(KVInfo.VAL_ROBOTSPAWN_TRIGGER))
			doAddRobot(rr = new RobotSpawnTrigger(this, rdef));
		else if(rClass.equals(KVInfo.VAL_CASTLEFLAG))
			doAddRobot(rr = new CastleFlag(this, rdef));
		else if(rClass.equals(KVInfo.VAL_MUSHROOM))
			doAddRobot(rr = new PowerMushroom(this, rdef));
		else if(rClass.equals(KVInfo.VAL_FIREFLOWER))
			doAddRobot(rr = new FireFlower(this, rdef));
		else if(rClass.equals(KVInfo.VAL_MUSH1UP))
			doAddRobot(rr = new Mush1UP(this, rdef));
		else if(rClass.equals(KVInfo.VAL_BRICKPIECE))
			doAddRobot(rr = new BrickPiece(this, rdef));
		else if(rClass.equals(KVInfo.VAL_SPINCOIN))
			doAddRobot(rr = new SpinCoin(this, rdef));
		else if(rClass.equals(KVInfo.VAL_MARIOFIREBALL))
			doAddRobot(rr = new MarioFireball(this, rdef));

		return rr;
	}

	public void enableRobotUpdate(RobotRole robo) {
		updateRobotEnableQ.add(robo);
	}

	public void disableRobotUpdate(RobotRole robo) {
		updateRobotDisableQ.add(robo);
	}

	public void setRobotDrawLayer(RobotRole robo, SpriteDrawOrder layer) {
		setRobotDrawLayerQ.add(new RobotDrawForQueue(robo, layer));
	}

	public void updateRobots(float delta) {
		for(RobotRole robot : updateRobots)
			robot.update(delta);

		// during update of robots, some robots may have been added to list of robots to add/destroy
		while(!robotAddQ.isEmpty())
			doAddRobot(robotAddQ.poll());
		while(!robotDestroyQ.isEmpty())
			doDestroyRobot(robotDestroyQ.poll());

		while(!updateRobotEnableQ.isEmpty())
			doEnableRobotUpdate(updateRobotEnableQ.poll());
		while(!updateRobotDisableQ.isEmpty())
			doDisableRobotUpdate(updateRobotDisableQ.poll());

		while(!setRobotDrawLayerQ.isEmpty())
			doSetRobotDrawLayer(setRobotDrawLayerQ.poll());
	}

	private void doAddRobot(RobotRole robo) {
		robots.add(robo);
	}

	private void doDestroyRobot(RobotRole robo) {
		robots.remove(robo);

		if(updateRobots.contains(robo))
			updateRobots.remove(robo);
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
			if(drawRobots[i].contains(robo))
				drawRobots[i].remove(robo);
		}

		robo.dispose();
	}

	private void doEnableRobotUpdate(RobotRole robo) {
		if(!updateRobots.contains(robo))
			updateRobots.add(robo);
	}

	private void doDisableRobotUpdate(RobotRole robo) {
		if(updateRobots.contains(robo))
			updateRobots.remove(robo);
	}

	private void doSetRobotDrawLayer(RobotDrawForQueue dRobo) {
		// check all layers for robo and remove if necessary
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
			if(drawRobots[i].contains(dRobo.robo))
				drawRobots[i].remove(dRobo.robo);
		}
		if(dRobo.drawOrder != SpriteDrawOrder.NONE)
			drawRobots[dRobo.drawOrder.ordinal()].add(dRobo.robo);
	}

	public void setPhysicTile(Vector2 t, boolean solid) {
		physicTileChangeQ.add(new PhysicTileForQueue((int) t.x, (int) t.y, solid));
	}

	// tile creates and destroys
	public void updateTileWorld(float delta) {
		while(!physicTileChangeQ.isEmpty()) {
			PhysicTileForQueue pTile = physicTileChangeQ.poll();
			if(pTile.solid == true)
				collisionMap.addTile(pTile.x, pTile.y);
			else
				collisionMap.removeTile(pTile.x, pTile.y);
		}
	}

	public boolean isMapTileSolid(Vector2 tilePos) {
		return collisionMap.isTileExist((int) tilePos.x, (int) tilePos.y);
	}

	public World getWorld() {
		return world;
	}

	public TileCollisionMap getCollisionMap() {
		return collisionMap;
	}

	// WorldRenderer will get and render the robots to draw
	public Collection<RobotRole>[] getRobotsToDraw() {
		return drawRobots;
	}

	public EncapTexAtlas getEncapTexAtlas() {
		return encapTexAtlas;
	}

	public void setRoleEventListener(RoleEventListener listener) {
		roleEventListener = listener;
	}

	public void playSound(String soundName) {
		if(roleEventListener != null)
			roleEventListener.onPlaySound(soundName);
	}

	public void startRoomMusic() {
		if(roleEventListener != null)
			roleEventListener.onStartRoomMusic();
	}

	public void stopRoomMusic() {
		if(roleEventListener != null)
			roleEventListener.onStopRoomMusic();
	}

	public void startSinglePlayMusic(String musicName) {
		if(roleEventListener != null)
			roleEventListener.onStartSinglePlayMusic(musicName);
	}

	public RobotRole getFirstRobotByProperties(String[] keys, String[] vals) {
		Collection<RobotRole> r = getRobotsByPropertiesInt(keys, vals, true);
		if(r.iterator().hasNext())
			return r.iterator().next(); 
		return null;
	}

	public Collection<RobotRole> getRobotsByProperties(String[] keys, String[] vals) {
		return getRobotsByPropertiesInt(keys, vals, false);
	}

	/*
	 * Never returns null. If no robot(s) are found, returns an empty collection.
	 */
	private Collection<RobotRole> getRobotsByPropertiesInt(String[] keys, String[] vals, boolean firstOnly) {
		LinkedList<RobotRole> ret = new LinkedList<RobotRole>();

		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");

		// loop through list of all robots, ignoring robots that have any wrong key/value pairs 
		for(RobotRole rr : robots) {
			boolean ignore = false;
			for(int i=0; i<keys.length; i++) {
				// If the key is not found, or the value doesn't match then ignore this robot (if the value 
				// to match is null then don't check value).
				if(!rr.getProperties().containsKey(keys[i]) ||
						(vals[i] != null && !rr.getProperties().get(keys[i], String.class).equals(vals[i]))) {
					ignore = true;
					break;
				}
			}
			if(ignore)
				continue;

			// this robot had all the right keys and values, so return it
			ret.add(rr);
			// return only first robot found?
			if(firstOnly)
				return ret;
		}
		return ret;
	}

	/*
	 * Returns null if player spawner not found
	 */
	public PlayerSpawner getPlayerSpawnerByName(String name) {
		RobotRole rr = getFirstRobotByProperties(new String[] { KVInfo.KEY_ROBOTROLECLASS, KVInfo.KEY_NAME },
				new String[] { KVInfo.VAL_SPAWNPLAYER, name });
		if(rr instanceof PlayerSpawner)
			return (PlayerSpawner) rr;
		return null;
	}

	/*
	 * Returns null if player spawner not found
	 */
	public PlayerSpawner getPlayerMainSpawner() {
		RobotRole rr = getFirstRobotByProperties(
				new String[] { KVInfo.KEY_ROBOTROLECLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNPLAYER, null });
		if(rr instanceof PlayerSpawner)
			return (PlayerSpawner) rr;
		return null;
	}

	public float getLevelTimeRemaining() {
		return levelTimeRemaining;
	}

	// This is a hack, TODO: the player controls a robot, so the player is not created here, it is
	// created in whatever class is using this class.
	public PlayerRole createPlayer(Vector2 position) {
		if(playerRole != null)
			throw new IllegalStateException("Cannot create new player while player already exists.");
		playerRole =  new MarioRole(this, position);
		return playerRole;
	}

	@Override
	public void dispose() {
		collisionMap.dispose();
		world.dispose();
	}
}
