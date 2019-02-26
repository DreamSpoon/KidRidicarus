package kidridicarus.agency.space;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.info.AgencyKV;

/*
 * Load .tmx files, basically.
 */
public class SpaceTemplateLoader {
	public static SpaceTemplate loadMap(String spaceFilename) {
		SpaceTemplate ret = new SpaceTemplate();
		TiledMap tiledMap = (new TmxMapLoader()).load(spaceFilename);
		ret.setMap(tiledMap);
		ret.addAgentDefs(makeAgentDefsFromLayers(tiledMap.getLayers()));
		return ret;
	}

	private static LinkedList<AgentDef> makeAgentDefsFromLayers(MapLayers layers) {
		LinkedList<AgentDef> agentDefs = new LinkedList<AgentDef>();
		for(MapLayer layer : layers)
			agentDefs.addAll(makeAgentDefsFromLayer(layer));
		return agentDefs;
	}

	private static LinkedList<AgentDef> makeAgentDefsFromLayer(MapLayer layer) {
		if(layer instanceof TiledMapTileLayer)
			return makeAgentDefsFromTileLayer((TiledMapTileLayer) layer);
		else
			return makeAgentDefsFromObjLayer(layer);
	}

	private static LinkedList<AgentDef> makeAgentDefsFromTileLayer(TiledMapTileLayer layer) {
		LinkedList<AgentDef> agentDefs = new LinkedList<AgentDef>();
		if(!layer.getProperties().containsKey(AgencyKV.Spawn.KEY_AGENTCLASS))
			return agentDefs;	// if no agentclass then return empty list of agent defs

		// create list of agent defs
		for(int y=0; y<layer.getHeight(); y++) {
			for(int x=0; x<layer.getWidth(); x++) {
				if(layer.getCell(x, y) == null || layer.getCell(x, y).getTile() == null)
					continue;
				agentDefs.add(makeAgentDef(UInfo.getP2MTileRect(x, y), layer.getProperties(),
						layer.getCell(x,  y).getTile().getTextureRegion()));
			}
		}
		return agentDefs;
	}

	private static LinkedList<AgentDef> makeAgentDefsFromObjLayer(MapLayer layer) {
		LinkedList<AgentDef> agentDefs = new LinkedList<AgentDef>();
		for(RectangleMapObject rect : layer.getObjects().getByType(RectangleMapObject.class)) {
			// combine the layer and object properties and pass to the agent def creator
			MapProperties combined = new MapProperties();
			combined.putAll(layer.getProperties());
			combined.putAll(rect.getProperties());
			agentDefs.add(makeAgentDef(UInfo.P2MRect(rect.getRectangle()), combined, null));
		}
		return agentDefs;
	}

	private static AgentDef makeAgentDef(Rectangle bounds, MapProperties properties, TextureRegion tileTexRegion) {
		AgentDef adef = new AgentDef();
		adef.bounds = bounds;
		adef.properties = properties;
		adef.tileTexRegion = tileTexRegion;
		return adef;
	}
}
