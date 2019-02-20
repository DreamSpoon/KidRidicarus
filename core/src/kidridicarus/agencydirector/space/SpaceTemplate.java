package kidridicarus.agencydirector.space;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import kidridicarus.agency.AgentDef;
import kidridicarus.info.GameInfo;
import kidridicarus.info.KVInfo;

public class SpaceTemplate {
	private LinkedList<AgentDef> agentDefs;
	private TiledMap tiledMap;
	private Collection<TiledMapTileLayer> solidTileLayers; 
	private Collection<MapLayer>[] drawLayers; 

	@SuppressWarnings("unchecked")
	public SpaceTemplate() {
		agentDefs = new LinkedList<AgentDef>();
		tiledMap = null;
		solidTileLayers = new LinkedList<TiledMapTileLayer>();
		// drawlayers sorted by order
		drawLayers = new LinkedList[GameInfo.LayerDrawOrder.values().length];
		for(int i=0; i<GameInfo.LayerDrawOrder.values().length; i++)
			drawLayers[i] = new LinkedList<MapLayer>();
	}

	public void setMap(TiledMap tiledMap) {
		this.tiledMap = tiledMap;
		sortLayers();
	}

	/*
	 * Sort and keep separate lists of solid and draw layers.
	 * Notes:
	 * -a layer can be a solid layer and a draw layer concurrently
	 * -only TiledMapTileLayer objects can be solid layers 
	 */
	private void sortLayers() {
		for(MapLayer layer : tiledMap.getLayers()) {
			// is solid layer property set to true and the layer is a tiled layer?
			if(layer instanceof TiledMapTileLayer && layer.getProperties().get(KVInfo.Layer.KEY_SOLIDLAYER,
					KVInfo.VAL_FALSE, String.class).equals(KVInfo.VAL_TRUE)) {
				TiledMapTileLayer tmtl = (TiledMapTileLayer) layer;
				solidTileLayers.add(tmtl);
			}
			// is draw order property set?
			String drawOrder = layer.getProperties().get(KVInfo.Layer.KEY_DRAWORDER, "", String.class);
			if(!drawOrder.equals("")) {
				// add the layer to the draw order layer array
				if(drawOrder.equals(KVInfo.Layer.VAL_BOTTOM))
					drawLayers[GameInfo.LayerDrawOrder.BOTTOM.ordinal()].add(layer);
				else if(drawOrder.equals(KVInfo.Layer.VAL_MIDDLE))
					drawLayers[GameInfo.LayerDrawOrder.MIDDLE.ordinal()].add(layer);
				else if(drawOrder.equals(KVInfo.Layer.VAL_TOP))
					drawLayers[GameInfo.LayerDrawOrder.TOP.ordinal()].add(layer);
				else if(drawOrder.equals(KVInfo.Layer.VAL_NONE))
					drawLayers[GameInfo.LayerDrawOrder.NONE.ordinal()].add(layer);
			}
		}
	}

	public Collection<TiledMapTileLayer> getSolidLayers() {
		return solidTileLayers;
	}

	public Collection<MapLayer>[] getDrawLayers() {
		return drawLayers;
	}

	public TiledMap getMap() {
		return tiledMap;
	}

	public List<AgentDef> getAgentDefs() {
		return agentDefs;
	}

	public void addAgentDefs(List<AgentDef> moreAgentDefs) {
		agentDefs.addAll(moreAgentDefs);
	}
}
