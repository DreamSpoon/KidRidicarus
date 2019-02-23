package kidridicarus.agencydirector.space;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import kidridicarus.agency.AgentDef;
import kidridicarus.info.KVInfo;
import kidridicarus.tool.DrawOrder;
import kidridicarus.tool.DrawOrderAlias;

public class SpaceTemplate {
	private LinkedList<AgentDef> agentDefs;
	private TiledMap tiledMap;
	private Collection<TiledMapTileLayer> solidTileLayers; 
	private TreeMap<DrawOrder, LinkedList<TiledMapTileLayer>> drawLayers;

	public SpaceTemplate() {
		agentDefs = new LinkedList<AgentDef>();
		tiledMap = null;
		solidTileLayers = new LinkedList<TiledMapTileLayer>();
		drawLayers = new TreeMap<DrawOrder, LinkedList<TiledMapTileLayer>>();
	}

	public void addAgentDefs(List<AgentDef> moreAgentDefs) {
		agentDefs.addAll(moreAgentDefs);
	}

	public List<AgentDef> getAgentDefs() {
		return agentDefs;
	}

	/*
	 * Set the map for this space template, and
	 * Sort and keep separate lists of solid and draw layers.
	 * Notes:
	 *   -a layer can be a solid layer and a draw layer concurrently
	 *   -only TiledMapTileLayer objects can be solid layers
	 *   -a layer may start with draw order none, and the draw order may change later - so add all layers
	 *    regardless of current draw order 
	 */
	public void setMap(TiledMap tiledMap) {
		this.tiledMap = tiledMap;
		// sort the layers
		for(MapLayer layer : tiledMap.getLayers()) {
			checkAndAddSolidLayer(layer);
			checkAndAddDrawLayer(layer);
		}
	}

	private void checkAndAddSolidLayer(MapLayer layer) {
		if(!(layer instanceof TiledMapTileLayer))
			return;
		// is solid layer property set to true and the layer is a tiled layer?
		if(layer.getProperties().get(KVInfo.Layer.KEY_SOLIDLAYER,
				KVInfo.VAL_FALSE, String.class).equals(KVInfo.VAL_TRUE)) {
			TiledMapTileLayer tmtl = (TiledMapTileLayer) layer;
			solidTileLayers.add(tmtl);
		}
	}

	/*
	 * If layer is a tiled map type, and it has a draw order, then add it to the draw order list based
	 * on it's draw order.
	 */
	private void checkAndAddDrawLayer(MapLayer layer) {
		if(!(layer instanceof TiledMapTileLayer))
			return;
		DrawOrder layerDO = getDrawOrderForLayer(layer);
		if(layerDO == null)
			return;

		// If a key does not exist for the layer's draw order then create a new list for the layer's
		// draw order key and add the layer to the list 
		LinkedList<TiledMapTileLayer> list = drawLayers.get(layerDO);
		if(list == null) {
			list = new LinkedList<TiledMapTileLayer>();
			drawLayers.put(layerDO, list);
		}
		list.add((TiledMapTileLayer) layer);
	}

	/*
	 * Returns null if draw order not found for given layer,
	 * otherwise returns a draw order object based on the layer's draw order property.
	 */
	private DrawOrder getDrawOrderForLayer(MapLayer layer) {
		// does the layer contain a draw order key with a float value?
		Float drawOrderFloat = null;
		try {
			drawOrderFloat = layer.getProperties().get(KVInfo.DrawOrder.KEY_DRAWORDER, null, Float.class);
		}
		catch(ClassCastException cce1) {
			// no float value, does the layer contain a draw order key with a string value?
			String drawOrderStr = null;
			try {
				drawOrderStr = layer.getProperties().get(KVInfo.DrawOrder.KEY_DRAWORDER, null, String.class);
			}
			catch(ClassCastException cce2) {
				// return null because no float value and no string found to indicate draw order for layer
				return null;
			}
			// check draw order aliases to translate to draw order object
			DrawOrderAlias doa = DrawOrderAlias.getByString(drawOrderStr);
			if(doa == null) {
				return null;
			}
			return doa.myDO;
		}
		if(drawOrderFloat == null)
			return null;
		return new DrawOrder(true, drawOrderFloat);
	}

	public Collection<TiledMapTileLayer> getSolidLayers() {
		return solidTileLayers;
	}

	public TreeMap<DrawOrder, LinkedList<TiledMapTileLayer>> getDrawLayers() {
		return drawLayers;
	}

	public TiledMap getMap() {
		return tiledMap;
	}
}
