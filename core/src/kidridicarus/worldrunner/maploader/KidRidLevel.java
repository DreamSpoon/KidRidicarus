package kidridicarus.worldrunner.maploader;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import kidridicarus.info.GameInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.worldrunner.RobotRoleDef;

public class KidRidLevel {
	private LinkedList<RobotRoleDef> robotDefs;
	private TiledMap map;
	private Collection<TiledMapTileLayer> solidLayers; 
	private Collection<MapLayer>[] drawLayers; 

	@SuppressWarnings("unchecked")
	public KidRidLevel() {
		robotDefs = new LinkedList<RobotRoleDef>();
		map = null;
		solidLayers = new LinkedList<TiledMapTileLayer>();
		// drawlayers sorted by order
		drawLayers = new LinkedList[GameInfo.LayerDrawOrder.values().length];
		for(int i=0; i<GameInfo.LayerDrawOrder.values().length; i++)
			drawLayers[i] = new LinkedList<MapLayer>();
	}

	public void setMap(TiledMap map) {
		this.map = map;
		sortLayers();
	}

	/*
	 * Find and keep separate lists of solid and draw layers.
	 * Notes:
	 * -a layer can be a solid layer and a draw layer concurrently
	 * -only TiledMapTileLayer objects can be solid layers 
	 */
	private void sortLayers() {
		for(MapLayer layer : map.getLayers()) {
			// is solid layer property set to true and the layer is a tiled layer?
			if(layer instanceof TiledMapTileLayer && layer.getProperties().get(KVInfo.KEY_SOLIDLAYER,
					KVInfo.VAL_FALSE, String.class).equals(KVInfo.VAL_TRUE)) {
				TiledMapTileLayer tmtl = (TiledMapTileLayer) layer;
				solidLayers.add(tmtl);
			}
			// is draw order property set?
			String drawOrder = layer.getProperties().get(KVInfo.KEY_DRAWORDER, "", String.class);
			if(!drawOrder.equals("")) {
				// add the layer to the draw order layer array
				if(drawOrder.equals(KVInfo.VAL_BOTTOM))
					drawLayers[GameInfo.LayerDrawOrder.BOTTOM.ordinal()].add(layer);
				else if(drawOrder.equals(KVInfo.VAL_MIDDLE))
					drawLayers[GameInfo.LayerDrawOrder.MIDDLE.ordinal()].add(layer);
				else if(drawOrder.equals(KVInfo.VAL_TOP))
					drawLayers[GameInfo.LayerDrawOrder.TOP.ordinal()].add(layer);
				else if(drawOrder.equals(KVInfo.VAL_NONE))
					drawLayers[GameInfo.LayerDrawOrder.NONE.ordinal()].add(layer);
			}
		}
	}

	public Collection<TiledMapTileLayer> getSolidLayers() {
		return solidLayers;
	}

	public Collection<MapLayer>[] getDrawLayers() {
		return drawLayers;
	}

	public TiledMap getMap() {
		return map;
	}

	public void addRobotDef(RobotRoleDef rd) {
		robotDefs.add(rd);
	}

	public LinkedList<RobotRoleDef> getRobotDefs() {
		return robotDefs;
	}

	public void addRobotDefs(LinkedList<RobotRoleDef> roboDefs) {
		robotDefs.addAll(roboDefs);
	}
}
