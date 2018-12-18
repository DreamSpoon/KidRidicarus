package kidridicarus.worldrunner.maploader;

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

import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;
import kidridicarus.worldrunner.RobotRoleDef;

public class GamemapLoader {
	public static KidRidLevel loadMap(String mapFilename) {
		KidRidLevel ret = new KidRidLevel();
		TiledMap map = (new TmxMapLoader()).load(mapFilename);
		ret.setMap(map);
		ret.addRobotDefs(loadRobotDefsFromLayers(map.getLayers()));
		return ret;
	}

	private static LinkedList<RobotRoleDef> loadRobotDefsFromLayers(MapLayers layers) {
		LinkedList<RobotRoleDef> robotDefs = new LinkedList<RobotRoleDef>();
		for(MapLayer layer : layers) {
			LinkedList<RobotRoleDef> check = checkLayerForRobotDefs(layer);
			if(check != null)
				robotDefs.addAll(check);
		}
		return robotDefs;
	}

	private static LinkedList<RobotRoleDef> checkLayerForRobotDefs(MapLayer layer) {
		// the layer needs the ROBOTROLECLASS key to unlock robot def creation
		if(!layer.getProperties().containsKey(KVInfo.KEY_ROBOTROLECLASS))
			return null;

		if(layer instanceof TiledMapTileLayer)
			return makeThingsFromTileLayer((TiledMapTileLayer) layer);
		else
			return makeThingsFromObjLayer(layer);
	}

	private static LinkedList<RobotRoleDef> makeThingsFromTileLayer(TiledMapTileLayer layer) {
		LinkedList<RobotRoleDef> robotDefs = new LinkedList<RobotRoleDef>();
		for(int y=0; y<layer.getHeight(); y++) {
			for(int x=0; x<layer.getWidth(); x++) {
				if(layer.getCell(x, y) == null || layer.getCell(x, y).getTile() == null)
					continue;
				robotDefs.add(createRobotDef(UInfo.getP2MTileRect(x, y), layer.getProperties(),
						layer.getCell(x,  y).getTile().getTextureRegion()));
			}
		}
		return robotDefs;
	}

	private static LinkedList<RobotRoleDef> makeThingsFromObjLayer(MapLayer layer) {
		LinkedList<RobotRoleDef> robotDefs = new LinkedList<RobotRoleDef>();
		for(MapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
			// combine the layer and object properties and pass to the robot def creator
			MapProperties combined = new MapProperties();
			combined.putAll(layer.getProperties());
			combined.putAll(object.getProperties());
			robotDefs.add(createRobotDef(UInfo.P2MRect(((RectangleMapObject) object).getRectangle()), combined, null));
		}
		return robotDefs;
	}

	private static RobotRoleDef createRobotDef(Rectangle bounds, MapProperties properties, TextureRegion tileTexRegion) {
		RobotRoleDef rdef = new RobotRoleDef();
		rdef.bounds = bounds;
		rdef.properties = properties;
		rdef.tileTexRegion = tileTexRegion;
		return rdef;
	}
}
