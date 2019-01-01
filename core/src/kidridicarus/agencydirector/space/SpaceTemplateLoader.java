package kidridicarus.agencydirector.space;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.AgentDef;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;

public class SpaceTemplateLoader {
	public static SpaceTemplate loadMap(String spaceFilename) {
		SpaceTemplate ret = new SpaceTemplate();
		TiledMap tiledMap = (new TmxMapLoader()).load(spaceFilename);
		ret.setMap(tiledMap);
		ret.addAgentDefs(loadAgentDefsFromLayers(tiledMap.getLayers()));
		return ret;
	}

	private static LinkedList<AgentDef> loadAgentDefsFromLayers(MapLayers layers) {
		LinkedList<AgentDef> agentDefs = new LinkedList<AgentDef>();
		for(MapLayer layer : layers) {
			LinkedList<AgentDef> check = checkLayerForAgentDefs(layer);
			if(check != null)
				agentDefs.addAll(check);
		}
		return agentDefs;
	}

	private static LinkedList<AgentDef> checkLayerForAgentDefs(MapLayer layer) {
		if(layer instanceof TiledMapTileLayer)
			return makeThingsFromTileLayer((TiledMapTileLayer) layer);
		else
			return makeThingsFromObjLayer(layer);
	}

	private static LinkedList<AgentDef> makeThingsFromTileLayer(TiledMapTileLayer layer) {
		LinkedList<AgentDef> agentDefs = new LinkedList<AgentDef>();
		if(!layer.getProperties().containsKey(KVInfo.KEY_AGENTCLASS))
			return agentDefs;	// if no agentclass then return empty list of agent defs

		// create list of agent defs
		for(int y=0; y<layer.getHeight(); y++) {
			for(int x=0; x<layer.getWidth(); x++) {
				if(layer.getCell(x, y) == null || layer.getCell(x, y).getTile() == null)
					continue;
				agentDefs.add(createAgentDef(UInfo.getP2MTileRect(x, y), layer.getProperties(),
						layer.getCell(x,  y).getTile().getTextureRegion()));
			}
		}
		return agentDefs;
	}

	private static LinkedList<AgentDef> makeThingsFromObjLayer(MapLayer layer) {
		LinkedList<AgentDef> agentDefs = new LinkedList<AgentDef>();
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
			// combine the layer and object properties and pass to the agent def creator
			MapProperties combined = new MapProperties();
			combined.putAll(layer.getProperties());
			combined.putAll(object.getProperties());
			agentDefs.add(createAgentDef(UInfo.P2MRect(((RectangleMapObject) object).getRectangle()), combined, null));
		}
		return agentDefs;
	}

	private static AgentDef createAgentDef(Rectangle bounds, MapProperties properties, TextureRegion tileTexRegion) {
		AgentDef adef = new AgentDef();
		adef.bounds = bounds;
		adef.properties = properties;
		adef.tileTexRegion = tileTexRegion;
		return adef;
	}
}
