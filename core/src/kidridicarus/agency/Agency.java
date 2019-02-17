package kidridicarus.agency;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.contact.WorldContactFilter;
import kidridicarus.agency.contact.WorldContactListener;
import kidridicarus.agent.Agent;
import kidridicarus.agent.Metroid.NPC.Skree;
import kidridicarus.agent.Metroid.NPC.SkreeExp;
import kidridicarus.agent.Metroid.NPC.Zoomer;
import kidridicarus.agent.Metroid.item.MaruMari;
import kidridicarus.agent.Metroid.player.Samus;
import kidridicarus.agent.Metroid.player.SamusShot;
import kidridicarus.agent.SMB.BrickPiece;
import kidridicarus.agent.SMB.BumpTile;
import kidridicarus.agent.SMB.CastleFlag;
import kidridicarus.agent.SMB.Flagpole;
import kidridicarus.agent.SMB.FloatingPoints;
import kidridicarus.agent.SMB.LevelEndTrigger;
import kidridicarus.agent.SMB.WarpPipe;
import kidridicarus.agent.SMB.NPC.Goomba;
import kidridicarus.agent.SMB.NPC.Turtle;
import kidridicarus.agent.SMB.SpinCoin;
import kidridicarus.agent.SMB.item.FireFlower;
import kidridicarus.agent.SMB.item.Mush1UP;
import kidridicarus.agent.SMB.item.PowerMushroom;
import kidridicarus.agent.SMB.item.PowerStar;
import kidridicarus.agent.SMB.item.StaticCoin;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.SMB.player.MarioFireball;
import kidridicarus.agent.general.DespawnBox;
import kidridicarus.agent.general.GuideSpawner;
import kidridicarus.agent.general.AgentSpawnTrigger;
import kidridicarus.agent.general.AgentSpawner;
import kidridicarus.agent.general.Room;
import kidridicarus.collisionmap.TileCollisionMap;
import kidridicarus.info.KVInfo;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.tools.BlockingQueueList;
import kidridicarus.tools.BlockingQueueList.AddRemCallback;

public class Agency implements Disposable {
	private World world;
	private TileCollisionMap collisionMap;

	private BlockingQueueList<Agent> allAgents;

	// sub-list of agents, just agents receiving updates
	private BlockingQueueList<Agent> allUpdateAgents;

	private class PhysTileItem {
		public int x;
		public int y;
		public boolean solid;
		public PhysTileItem(int x, int y, boolean solid) {
			this.x = x;
			this.y = y;
			this.solid = solid;
		}
	}
	private LinkedBlockingQueue<PhysTileItem> physicTileChangeQ;

	private class AgentDrawOrderItem {
		public Agent agent;
		public SpriteDrawOrder drawOrder;
		public AgentDrawOrderItem(Agent agent, SpriteDrawOrder drawOrder) {
			this.agent = agent;
			this.drawOrder = drawOrder;
		}
	}
	// sub-list of agents, just agents being drawn
	private LinkedList<Agent>[] drawAgents;
	private LinkedBlockingQueue<AgentDrawOrderItem> setAgentDrawLayerQ;

	private TextureAtlas atlas;
	private AgencyEventListener agencyEventListener;

	private class AllAgentsAddRem implements AddRemCallback<Agent> {
		@Override
		public void add(Agent agent) {}
		@Override
		public void remove(Agent agent) {
			// check list of agentss to update, remove agent if found
			if(allUpdateAgents.contains(agent))
				allUpdateAgents.remove(agent);
			// check lists of agents to draw, remove agent if found
			for(int i=0; i<SpriteDrawOrder.values().length; i++) {
				if(drawAgents[i].contains(agent))
					drawAgents[i].remove(agent);
			}
			agent.dispose();
		}
	}

	private Object[] agentClassList = new Object[] {
			KVInfo.VAL_BUMPTILE, BumpTile.class,
			KVInfo.VAL_GOOMBA, Goomba.class,
			KVInfo.VAL_TURTLE, Turtle.class,
			KVInfo.VAL_COIN, StaticCoin.class,
			KVInfo.VAL_AGENTSPAWNER, AgentSpawner.class,
			KVInfo.VAL_SPAWNGUIDE, GuideSpawner.class,
			KVInfo.VAL_ROOM, Room.class,
			KVInfo.VAL_FLAGPOLE, Flagpole.class,
			KVInfo.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			KVInfo.VAL_PIPEWARP, WarpPipe.class,
			KVInfo.VAL_DESPAWN, DespawnBox.class,
			KVInfo.VAL_AGENTSPAWN_TRIGGER, AgentSpawnTrigger.class,
			KVInfo.VAL_CASTLEFLAG, CastleFlag.class,
			KVInfo.VAL_MUSHROOM, PowerMushroom.class,
			KVInfo.VAL_FIREFLOWER, FireFlower.class,
			KVInfo.VAL_MUSH1UP, Mush1UP.class,
			KVInfo.VAL_POWERSTAR, PowerStar.class,
			KVInfo.VAL_BRICKPIECE, BrickPiece.class,
			KVInfo.VAL_SPINCOIN, SpinCoin.class,
			KVInfo.VAL_MARIOFIREBALL, MarioFireball.class,
			KVInfo.VAL_FLOATINGPOINTS, FloatingPoints.class,
			KVInfo.VAL_MARIO, Mario.class,
			KVInfo.VAL_ZOOMER, Zoomer.class,
			KVInfo.VAL_SKREE, Skree.class,
			KVInfo.VAL_SKREE_EXP, SkreeExp.class,
			KVInfo.VAL_MARUMARI, MaruMari.class,
			KVInfo.VAL_SAMUS, Samus.class,
			KVInfo.VAL_SAMUS_SHOT, SamusShot.class
		};

	private float globalTimer;

	@SuppressWarnings("unchecked")
	public Agency() {
		atlas = null;

		globalTimer = 0f;

		physicTileChangeQ = new LinkedBlockingQueue<PhysTileItem>();

		allAgents = new BlockingQueueList<Agent>(new AllAgentsAddRem());
		allUpdateAgents = new BlockingQueueList<Agent>();
		drawAgents = (LinkedList<Agent>[]) new LinkedList[SpriteDrawOrder.values().length];
		for(int i=0; i<SpriteDrawOrder.values().length; i++)
			drawAgents[i] = new LinkedList<Agent>();
		setAgentDrawLayerQ = new LinkedBlockingQueue<AgentDrawOrderItem>();

		world = new World(new Vector2(0, -10f), true);
		world.setContactListener(new WorldContactListener());
		world.setContactFilter(new WorldContactFilter());
	}

	public void setAtlas(TextureAtlas atlas) {
		this.atlas = atlas;
	}

	// uses the input layers to find solid (i.e. non-null) tiles and create the collision map
	public void createCollisionMap(Collection<TiledMapTileLayer> solidLayers) {
		collisionMap = new TileCollisionMap(world, solidLayers);
	}

	public void update(float delta) {
		world.step(delta, 6, 2);

		updateAgents(delta);
		updateTileWorld(delta);

		globalTimer += delta;
	}

	// TODO: If the World.step method is NOT running at present then dispose the agent immediately, otherwise add it
	// to the queue to be disposed later - apply this same concept to other queues if possible.
	public void disposeAgent(Agent agent) {
		allAgents.remove(agent);
	}

	public void createAgents(Collection<AgentDef> agentDefs) {
		Iterator<AgentDef> adIter = agentDefs.iterator();
		while(adIter.hasNext()) {
			AgentDef aDef = adIter.next();
			createAgent(aDef);
		}
	}

	/*
	 * Create an agent based on the adef.
	 * See website:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-create-an-object-via-its-multiparameter-constructor-using-reflection.html
	 */
	public Agent createAgent(AgentDef adef) {
		String desiredAgentClass = adef.properties.get(KVInfo.KEY_AGENTCLASS, String.class);

		Class<?> agentClass = null;
		for(int i=0; i<agentClassList.length; i+=2) {
			String agentClassName = (String) agentClassList[i+0];
			if(agentClassName.equals(desiredAgentClass)) {
				agentClass = (Class<?>) agentClassList[i+1];
				break;
			}
		}
		if(agentClass == null) {
			return null;
		}

		Constructor<?> constructor;
		Agent newlyCreatedAgent = null;
		try {
			constructor = agentClass.getConstructor(new Class[] { Agency.class, AgentDef.class });
			newlyCreatedAgent = (Agent) constructor.newInstance(new Object[] { this, adef });
		} catch (Exception e) {
			e.printStackTrace();
		}

		allAgents.add(newlyCreatedAgent);
		return newlyCreatedAgent;
	}

	public void enableAgentUpdate(Agent agent) {
		allUpdateAgents.add(agent);
	}

	public void disableAgentUpdate(Agent agent) {
		allUpdateAgents.remove(agent);
	}

	public void setAgentDrawLayer(Agent agent, SpriteDrawOrder layer) {
		setAgentDrawLayerQ.add(new AgentDrawOrderItem(agent, layer));
	}

	public void updateAgents(float delta) {
		for(Agent a : allUpdateAgents.getList())
			a.update(delta);

		// during update of agents, some agents may have been added to agents add/destroy queue, process the queues
		allAgents.processQ();
		allUpdateAgents.processQ();

		while(!setAgentDrawLayerQ.isEmpty())
			doSetAgentDrawLayer(setAgentDrawLayerQ.poll());
	}

	private void doSetAgentDrawLayer(AgentDrawOrderItem adoi) {
		// check all layers for agent and remove if necessary
		for(int i=0; i<SpriteDrawOrder.values().length; i++) {
			if(drawAgents[i].contains(adoi.agent))
				drawAgents[i].remove(adoi.agent);
		}
		if(adoi.drawOrder != SpriteDrawOrder.NONE)
			drawAgents[adoi.drawOrder.ordinal()].add(adoi.agent);
	}

	public void setPhysicTile(Vector2 t, boolean solid) {
		physicTileChangeQ.add(new PhysTileItem((int) t.x, (int) t.y, solid));
	}

	// tile creates and destroys
	public void updateTileWorld(float delta) {
		while(!physicTileChangeQ.isEmpty()) {
			PhysTileItem pTile = physicTileChangeQ.poll();
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

	// WorldRenderer will get and render the agents to draw
	public Collection<Agent>[] getAgentsToDraw() {
		return drawAgents;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public void setEventListener(AgencyEventListener listener) {
		agencyEventListener = listener;
	}

	public void playSound(String soundName) {
		if(agencyEventListener != null)
			agencyEventListener.onPlaySound(soundName);
	}

	public void changeAndStartMusic(String musicName) {
		if(agencyEventListener != null)
			agencyEventListener.onChangeAndStartMusic(musicName);
	}

	public void startMusic() {
		if(agencyEventListener != null)
			agencyEventListener.onStartMusic();
	}

	public void stopMusic() {
		if(agencyEventListener != null)
			agencyEventListener.onStopMusic();
	}

	public void startSinglePlayMusic(String musicName) {
		if(agencyEventListener != null)
			agencyEventListener.onStartSinglePlayMusic(musicName);
	}

	public Agent getFirstAgentByProperties(String[] keys, String[] vals) {
		Collection<Agent> r = getAgentsByPropertiesInt(keys, vals, true);
		if(r.iterator().hasNext())
			return r.iterator().next(); 
		return null;
	}

	public Collection<Agent> getAgentsByProperties(String[] keys, String[] vals) {
		return getAgentsByPropertiesInt(keys, vals, false);
	}

	/*
	 * Never returns null. If no agent(s) are found, returns an empty collection.
	 */
	private Collection<Agent> getAgentsByPropertiesInt(String[] keys, String[] vals, boolean firstOnly) {
		LinkedList<Agent> ret = new LinkedList<Agent>();

		if(keys.length != vals.length)
			throw new IllegalArgumentException("keys[] and vals[] arrays are not of equal length.");

		// loop through list of all agents, ignoring agents that have any wrong key/value pairs 
		for(Agent a : allAgents.getList()) {
			boolean ignore = false;
			for(int i=0; i<keys.length; i++) {
				// If the key is not found, or the value doesn't match then ignore this agent (if the value 
				// to match is null then don't check value).
				if(!a.getProperties().containsKey(keys[i]) ||
						(vals[i] != null && !a.getProperties().get(keys[i], String.class).equals(vals[i]))) {
					ignore = true;
					break;
				}
			}
			if(ignore)
				continue;

			// this agent had all the right keys and values, so return it
			ret.add(a);
			// return only first agent found?
			if(firstOnly)
				return ret;
		}
		return ret;
	}

	/*
	 * Returns null if guide spawner not found
	 */
	public GuideSpawner getGuideSpawnerByName(String name) {
		Agent agent = getFirstAgentByProperties(new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_NAME },
				new String[] { KVInfo.VAL_SPAWNGUIDE, name });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	/*
	 * Returns null if guide spawner not found
	 */
	public GuideSpawner getGuideMainSpawner() {
		Agent agent = getFirstAgentByProperties(
				new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNGUIDE, null });
		if(agent instanceof GuideSpawner)
			return (GuideSpawner) agent;
		return null;
	}

	public float getGlobalTimer() {
		return globalTimer;
	}

	@Override
	public void dispose() {
		collisionMap.dispose();
		world.dispose();
	}
}
