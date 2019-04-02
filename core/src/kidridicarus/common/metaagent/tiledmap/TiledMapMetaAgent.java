package kidridicarus.common.metaagent.tiledmap;

import java.util.LinkedList;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.drawlayer.DrawLayerAgent;
import kidridicarus.common.metaagent.tiledmap.solidlayer.SolidTiledMapAgent;

/*
 * A "parent" or "meta" agent that has a solid tile map, drawable layers, and a batch of initial spawn agents.
 * This agent does not load anything from file, rather it is given a TiledMap object that has been preloaded.
 */
public class TiledMapMetaAgent extends Agent implements DisposableAgent {
	private TiledMap tiledMap;
	private SolidTiledMapAgent solidTileMapAgent;
	private LinkedList<DrawLayerAgent> drawLayerAgents;
	private Rectangle bounds;
	private boolean otherSpawnDone;

	public TiledMapMetaAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		this.bounds = Agent.getStartBounds(properties);

		tiledMap = properties.get(CommonKV.AgentMapParams.KEY_TILEDMAP, null, TiledMap.class);
		if(tiledMap == null)
			throw new IllegalArgumentException("Tiled map property not set, unable to create agent.");

		createInitialSubAgents();

		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(); }
			});
		otherSpawnDone = false;
	}

	// create the Agents for the solid tile map and the drawable layers
	private void createInitialSubAgents() {
		// get lists of solid and draw layers (there may be overlap between the two, that's okay)
		LinkedList<TiledMapTileLayer> solidLayers = new LinkedList<TiledMapTileLayer>(); 
		LinkedList<TiledMapTileLayer> drawLayers = new LinkedList<TiledMapTileLayer>(); 
		for(MapLayer layer : tiledMap.getLayers()) {
			if(!(layer instanceof TiledMapTileLayer))
				continue;

			// is solid layer property set to true?
			if(layer.getProperties().get(CommonKV.Layer.KEY_SOLIDLAYER,
					CommonKV.VAL_FALSE, String.class).equals(CommonKV.VAL_TRUE)) {
				solidLayers.add((TiledMapTileLayer) layer);
			}
			// does this layer have a draw order?
			if(layer.getProperties().get(CommonKV.DrawOrder.KEY_DRAWORDER, null, String.class) != null)
				drawLayers.add((TiledMapTileLayer) layer);
		}

		createSolidTileMapAgent(solidLayers);
		createDrawLayerAgents(drawLayers);
	}

	private void createSolidTileMapAgent(LinkedList<TiledMapTileLayer> solidLayers) {
		if(solidLayers.isEmpty())
			return;
		ObjectProperties cmProps = Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_ORTHO_SOLID_TILEMAP,
				Agent.getStartBounds(properties));
		cmProps.put(CommonKV.AgentMapParams.KEY_TILEDMAPTILELAYER_LIST, solidLayers);
		solidTileMapAgent = (SolidTiledMapAgent) agency.createAgent(cmProps);
	}

	private void createDrawLayerAgents(LinkedList<TiledMapTileLayer> drawLayers) {
		if(drawLayers.isEmpty())
			return;
		drawLayerAgents = new LinkedList<DrawLayerAgent>();
		// loop through each draw layer in the list, adding each and passing a ref to its tiled map layer
		for(TiledMapTileLayer layer : drawLayers) {
			ObjectProperties cmProps = Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_DRAWABLE_TILEMAP,
					Agent.getStartBounds(properties));
			cmProps.put(CommonKV.AgentMapParams.KEY_TILEDMAPTILELAYER, layer);
			drawLayerAgents.add((DrawLayerAgent) agency.createAgent(cmProps));
		}
	}

	/*
	 * Create other spawn agents on first update because the solid stuff in the map will have been created:
	 *   -when the Agency was created, before any update frames have been run, the map agent was created
	 *   -in the previous update
	 * So there was previously no solid stuff for the initial spawn agents to land on, so they needed to be
	 * spawned later.
	 */
	private void doUpdate() {
		if(!otherSpawnDone) {
			otherSpawnDone = true;
			// create the other agents (typically spawn boxes, rooms, player start, etc.)
			LinkedList<ObjectProperties> temp = makeAgentPropsFromLayers(tiledMap.getLayers());
			agency.createAgents(temp);
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
		if(!layer.getProperties().containsKey(AgencyKV.Spawn.KEY_AGENTCLASS))
			return agentProps;	// if no agentclass then return empty list of agent properties

		// create list of AgentProperties objects with some tile info and info from the layer's MapProperties
		for(int y=0; y<layer.getHeight(); y++) {
			for(int x=0; x<layer.getWidth(); x++) {
				if(layer.getCell(x, y) == null || layer.getCell(x, y).getTile() == null)
					continue;
				agentProps.add(Agent.createTileAP(layer.getProperties(), UInfo.getP2MTileRect(x, y),
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
			agentProps.add(Agent.createRectangleAP(combined, UInfo.P2MRect(rect.getRectangle())));
		}
		return agentProps;
	}

	@Override
	public Vector2 getPosition() {
		return bounds.getCenter(new Vector2());
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void disposeAgent() {
		if(solidTileMapAgent != null)
			solidTileMapAgent.dispose();
		if(tiledMap != null)
			tiledMap.dispose();
	}

	public static ObjectProperties makeAP(TiledMap tiledMap) {
		int width = tiledMap.getProperties().get(CommonKV.TiledMap.KEY_WIDTH, 0, Integer.class);
		int height = tiledMap.getProperties().get(CommonKV.TiledMap.KEY_HEIGHT, 0, Integer.class);
		if(width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Cannot create map agent from tiledMap when width or height" +
					"is not positive: width = " + width + ", height = " + height);
		}

		ObjectProperties props = Agent.createRectangleAP(CommonKV.AgentClassAlias.VAL_TILEMAP_META,
				new Rectangle(0f, 0f, UInfo.P2M(width * UInfo.TILEPIX_X), UInfo.P2M(height * UInfo.TILEPIX_Y)));
		props.put(CommonKV.AgentMapParams.KEY_TILEDMAP, tiledMap);

		return props;
	}
}
