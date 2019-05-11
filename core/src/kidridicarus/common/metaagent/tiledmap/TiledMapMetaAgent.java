package kidridicarus.common.metaagent.tiledmap;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.drawlayer.DrawLayerAgent;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;
import kidridicarus.common.tool.AP_Tool;

/*
 * A "parent" or "meta" agent that has a solid tile map, drawable layers, and a batch of initial spawn agents.
 * This agent does not load anything from file, rather it is given a TiledMap object that has been preloaded.
 */
public class TiledMapMetaAgent extends Agent implements Disposable {
	private TiledMap map;
	private SolidTiledMapAgent solidTileMapAgent;
	private LinkedList<DrawLayerAgent> drawLayerAgents;
	private LinkedList<Disposable> manualDisposeAgents;
	private AgentUpdateListener myUpdateListener;

	public TiledMapMetaAgent(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);

		manualDisposeAgents = new LinkedList<Disposable>();

		map = properties.get(CommonKV.AgentMapParams.KEY_TILEDMAP, null, TiledMap.class);
		if(map == null)
			throw new IllegalArgumentException("Tiled map property not set, unable to create agent.");
		createInitialSubAgents(AP_Tool.getBounds(properties));

		// keep ref to update listener for removal
		myUpdateListener = new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { doUpdate(); }
			};
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.MOVE_UPDATE, myUpdateListener);
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	// create the Agents for the solid tile map and the drawable layers
	private void createInitialSubAgents(Rectangle bounds) {
		// get lists of solid and draw layers (there may be overlap between the two, that's okay)
		LinkedList<TiledMapTileLayer> solidLayers = new LinkedList<TiledMapTileLayer>(); 
		LinkedList<TiledMapTileLayer> drawLayers = new LinkedList<TiledMapTileLayer>(); 
		for(MapLayer layer : map.getLayers()) {
			if(!(layer instanceof TiledMapTileLayer))
				continue;

			// is solid layer property set to true?
			if(layer.getProperties().get(CommonKV.Layer.KEY_LAYER_SOLID,
					CommonKV.VAL_FALSE, String.class).equals(CommonKV.VAL_TRUE)) {
				solidLayers.add((TiledMapTileLayer) layer);
			}
			// does this layer have a draw order?
			if(layer.getProperties().get(CommonKV.Layer.KEY_LAYER_DRAWORDER, null, String.class) != null)
				drawLayers.add((TiledMapTileLayer) layer);
		}

		createSolidTileMapAgent(bounds, solidLayers);
		createDrawLayerAgents(drawLayers);
	}

	private void createSolidTileMapAgent(Rectangle bounds, LinkedList<TiledMapTileLayer> solidLayers) {
		if(solidLayers.isEmpty())
			return;

		solidTileMapAgent = (SolidTiledMapAgent) agentHooks.createAgent(
				SolidTiledMapAgent.makeAP(bounds, solidLayers));
	}

	private void createDrawLayerAgents(LinkedList<TiledMapTileLayer> drawLayers) {
		if(drawLayers.isEmpty())
			return;
		drawLayerAgents = new LinkedList<DrawLayerAgent>();
		// loop through each draw layer in the list, adding each and passing a ref to its tiled map layer
		for(TiledMapTileLayer layer : drawLayers) {
			drawLayerAgents.add((DrawLayerAgent) agentHooks.createAgent(
					DrawLayerAgent.makeAP(getProperty(CommonKV.KEY_BOUNDS, null, Rectangle.class), layer)));
		}
	}

	/*
	 * Create other spawn agents on first update because the solid stuff in the map will have been created:
	 *   -when the Agency was created, before any update frames have been run, the map agent was created
	 *   -in the previous update
	 * So there was previously no solid stuff for the initial spawn agents to land on, so they needed to be
	 * spawned later.
	 * Note: Only one update, then update listener is removed.
	 */
	private void doUpdate() {
		agentHooks.removeUpdateListener(myUpdateListener);
		createOtherAgents();
	}

	/*
	 * Non-disposable Agents (e.g. KeepAliveBox) may be created by this Agent, so keep a list of Agents for
	 * manual disposal.
	 */
	private void createOtherAgents() {
		// create the other agents (typically spawn boxes, rooms, player start, etc.)
		List<Agent> createdAgents = agentHooks.createAgents(makeAgentPropsFromLayers(map.getLayers()));
		for(Agent agent : createdAgents) {
			if(agent instanceof Disposable)
				manualDisposeAgents.add((Disposable) agent);
		}
	}

	private static LinkedList<ObjectProperties> makeAgentPropsFromLayers(MapLayers layers) {
		LinkedList<ObjectProperties> agentProps = new LinkedList<ObjectProperties>();
		for(MapLayer layer : layers)
			agentProps.addAll(makeAgentPropsFromLayer(layer));
		return agentProps;
	}

	private static LinkedList<ObjectProperties> makeAgentPropsFromLayer(MapLayer layer) {
		if(layer instanceof TiledMapTileLayer)
			return makeAgentPropsFromTileLayer((TiledMapTileLayer) layer);
		else
			return makeAgentPropsFromObjLayer(layer);
	}

	private static LinkedList<ObjectProperties> makeAgentPropsFromTileLayer(TiledMapTileLayer layer) {
		LinkedList<ObjectProperties> agentProps = new LinkedList<ObjectProperties>();
		if(!layer.getProperties().containsKey(AgencyKV.KEY_AGENT_CLASS))
			return agentProps;	// if no agentclass then return empty list of agent properties

		// create list of AgentProperties objects with some tile info and info from the layer's MapProperties
		for(int y=0; y<layer.getHeight(); y++) {
			for(int x=0; x<layer.getWidth(); x++) {
				// only spawn an agent if the cell exists and an agent class key/value is available
				if(layer.getCell(x, y) == null || layer.getCell(x, y).getTile() == null ||
						!layer.getProperties().containsKey(AgencyKV.KEY_AGENT_CLASS))
					continue;

				agentProps.add(AP_Tool.createTileAP(layer.getProperties(), UInfo.RectangleT2M(x, y),
						layer.getCell(x,  y).getTile().getTextureRegion()));
			}
		}
		return agentProps;
	}

	private static LinkedList<ObjectProperties> makeAgentPropsFromObjLayer(MapLayer layer) {
		LinkedList<ObjectProperties> agentProps = new LinkedList<ObjectProperties>();
		for(RectangleMapObject rect : layer.getObjects().getByType(RectangleMapObject.class)) {
			// combine the layer and object properties and pass to the agent properties creator
			MapProperties combined = new MapProperties();
			combined.putAll(layer.getProperties());
			combined.putAll(rect.getProperties());
			// only spawn an agent if an agent class key/value is available
			if(combined.containsKey(AgencyKV.KEY_AGENT_CLASS))
				agentProps.add(AP_Tool.createRectangleAP(combined, UInfo.RectangleP2M(rect.getRectangle())));
		}
		return agentProps;
	}

	@Override
	public void dispose() {
		for(Disposable agent: manualDisposeAgents)
			agent.dispose();
		if(solidTileMapAgent != null)
			solidTileMapAgent.dispose();
		if(map != null)
			map.dispose();
		// Draw layer agents do not need to be disposed (currently) because they do not release gfx memory,
		// (the tilemap releases the memory) - and the draw layer agents do not have Box2D bodies (currently).
	}

	public static ObjectProperties makeAP(TiledMap tiledMap) {
		int width = tiledMap.getProperties().get(CommonKV.TiledMap.KEY_WIDTH, 0, Integer.class);
		int height = tiledMap.getProperties().get(CommonKV.TiledMap.KEY_HEIGHT, 0, Integer.class);
		if(width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Cannot create map agent from tiledMap when width or height" +
					"is not positive: width = " + width + ", height = " + height);
		}
		ObjectProperties props = AP_Tool.createRectangleAP(CommonKV.AgentClassAlias.VAL_META_TILEDMAP,
				new Rectangle(0f, 0f, UInfo.P2M(width * UInfo.TILEPIX_X), UInfo.P2M(height * UInfo.TILEPIX_Y)));
		props.put(CommonKV.AgentMapParams.KEY_TILEDMAP, tiledMap);
		return props;
	}
}
