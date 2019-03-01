package kidridicarus.agency.space;

import java.util.LinkedList;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentProperties;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.DrawOrderAlias;
import kidridicarus.agency.info.AgencyKV;

/*
 * Load .tmx files, basically.
 */
public class SpaceTemplateLoader {
	public static SpaceTemplate loadMap(String spaceFilename, DrawOrderAlias[] drawOrderAliasList) {
		SpaceTemplate ret = new SpaceTemplate();
		TiledMap tiledMap = (new TmxMapLoader()).load(spaceFilename);
		ret.setMap(tiledMap, drawOrderAliasList);
		ret.addAgentProps(makeAgentPropsFromLayers(tiledMap.getLayers()));
		return ret;
	}

	private static LinkedList<AgentProperties> makeAgentPropsFromLayers(MapLayers layers) {
		LinkedList<AgentProperties> agentProps = new LinkedList<AgentProperties>();
		for(MapLayer layer : layers)
			agentProps.addAll(makeAgentPropsFromLayer(layer));
		return agentProps;
	}

	private static LinkedList<AgentProperties> makeAgentPropsFromLayer(MapLayer layer) {
		if(layer instanceof TiledMapTileLayer)
			return makeAgentPropsFromTileLayer((TiledMapTileLayer) layer);
		else
			return makeAgentPropsFromObjLayer(layer);
	}

	private static LinkedList<AgentProperties> makeAgentPropsFromTileLayer(TiledMapTileLayer layer) {
		LinkedList<AgentProperties> agentProps = new LinkedList<AgentProperties>();
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

	private static LinkedList<AgentProperties> makeAgentPropsFromObjLayer(MapLayer layer) {
		LinkedList<AgentProperties> agentProps = new LinkedList<AgentProperties>();
		for(RectangleMapObject rect : layer.getObjects().getByType(RectangleMapObject.class)) {
			// combine the layer and object properties and pass to the agent properties creator
			MapProperties combined = new MapProperties();
			combined.putAll(layer.getProperties());
			combined.putAll(rect.getProperties());
			agentProps.add(Agent.createRectangleAP(combined, UInfo.P2MRect(rect.getRectangle())));
		}
		return agentProps;
	}
}
