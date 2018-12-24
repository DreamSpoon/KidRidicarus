package kidridicarus.agency;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.contacts.WorldContactFilter;
import kidridicarus.agency.contacts.WorldContactListener;
import kidridicarus.agent.Agent;
import kidridicarus.agent.Metroid.enemy.Skree;
import kidridicarus.agent.Metroid.enemy.Zoomer;
import kidridicarus.agent.SMB.BrickPiece;
import kidridicarus.agent.SMB.BumpTile;
import kidridicarus.agent.SMB.CastleFlag;
import kidridicarus.agent.SMB.Flagpole;
import kidridicarus.agent.SMB.FloatingPoints;
import kidridicarus.agent.SMB.LevelEndTrigger;
import kidridicarus.agent.SMB.PipeWarp;
import kidridicarus.agent.SMB.SpinCoin;
import kidridicarus.agent.SMB.enemy.Goomba;
import kidridicarus.agent.SMB.enemy.Turtle;
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
import kidridicarus.tools.EncapTexAtlas;
import kidridicarus.tools.QQ;

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

	private EncapTexAtlas encapTexAtlas;
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

	@SuppressWarnings("unchecked")
	public Agency() {
		encapTexAtlas = null;

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

	public void setEncapTexAtlas(EncapTexAtlas encapTexAtlas) {
		this.encapTexAtlas = encapTexAtlas;
	}

	// uses the input layers to find solid (i.e. non-null) tiles and create the collision map
	public void createCollisionMap(Collection<TiledMapTileLayer> solidLayers) {
		collisionMap = new TileCollisionMap(world, solidLayers);
	}

	public void update(float delta) {
		world.step(delta, 6, 2);

		updateAgents(delta);
		updateTileWorld(delta);
	}

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

	public Agent createAgent(AgentDef adef) {
		String rClass = adef.properties.get(KVInfo.KEY_AGENTCLASS, String.class);

		Agent agent = null;
		if(rClass.equals(KVInfo.VAL_BUMPTILE))
			allAgents.add(agent = new BumpTile(this, adef));
		else if(rClass.equals(KVInfo.VAL_GOOMBA))
			allAgents.add(agent = new Goomba(this, adef));
		else if(rClass.equals(KVInfo.VAL_TURTLE))
			allAgents.add(agent = new Turtle(this, adef));
		else if(rClass.equals(KVInfo.VAL_COIN))
			allAgents.add(agent = new StaticCoin(this, adef));
		else if(rClass.equals(KVInfo.VAL_SPAWNGOOMBA) || rClass.equals(KVInfo.VAL_SPAWNTURTLE))
			allAgents.add(agent = new AgentSpawner(this, adef));
		else if(rClass.equals(KVInfo.VAL_SPAWNGUIDE))
			allAgents.add(agent = new GuideSpawner(this, adef));
		else if(rClass.equals(KVInfo.VAL_ROOM))
			allAgents.add(agent = new Room(this, adef));
		else if(rClass.equals(KVInfo.VAL_FLAGPOLE))
			allAgents.add(agent = new Flagpole(this, adef));
		else if(rClass.equals(KVInfo.VAL_LEVELEND_TRIGGER))
			allAgents.add(agent = new LevelEndTrigger(this, adef));
		else if(rClass.equals(KVInfo.VAL_PIPEWARP))
			allAgents.add(agent = new PipeWarp(this, adef));
		else if(rClass.equals(KVInfo.VAL_DESPAWN))
			allAgents.add(agent = new DespawnBox(this, adef));
		else if(rClass.equals(KVInfo.VAL_AGENTSPAWN_TRIGGER))
			allAgents.add(agent = new AgentSpawnTrigger(this, adef));
		else if(rClass.equals(KVInfo.VAL_CASTLEFLAG))
			allAgents.add(agent = new CastleFlag(this, adef));
		else if(rClass.equals(KVInfo.VAL_MUSHROOM))
			allAgents.add(agent = new PowerMushroom(this, adef));
		else if(rClass.equals(KVInfo.VAL_FIREFLOWER))
			allAgents.add(agent = new FireFlower(this, adef));
		else if(rClass.equals(KVInfo.VAL_MUSH1UP))
			allAgents.add(agent = new Mush1UP(this, adef));
		else if(rClass.equals(KVInfo.VAL_POWERSTAR))
			allAgents.add(agent = new PowerStar(this, adef));
		else if(rClass.equals(KVInfo.VAL_BRICKPIECE))
			allAgents.add(agent = new BrickPiece(this, adef));
		else if(rClass.equals(KVInfo.VAL_SPINCOIN))
			allAgents.add(agent = new SpinCoin(this, adef));
		else if(rClass.equals(KVInfo.VAL_MARIOFIREBALL))
			allAgents.add(agent = new MarioFireball(this, adef));
		else if(rClass.equals(KVInfo.VAL_FLOATINGPOINTS))
			allAgents.add(agent = new FloatingPoints(this, adef));
		else if(rClass.equals(KVInfo.VAL_MARIO))
			allAgents.add(agent = new Mario(this, adef));
		else if(rClass.equals(KVInfo.VAL_ZOOMER))
			allAgents.add(agent = new Zoomer(this, adef));
		else if(rClass.equals(KVInfo.VAL_SKREE))
			allAgents.add(agent = new Skree(this, adef));
		else
			QQ.pr("Unknown agent class to create: " + rClass);

		return agent;
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

	public EncapTexAtlas getEncapTexAtlas() {
		return encapTexAtlas;
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

	@Override
	public void dispose() {
		collisionMap.dispose();
		world.dispose();
	}
}
