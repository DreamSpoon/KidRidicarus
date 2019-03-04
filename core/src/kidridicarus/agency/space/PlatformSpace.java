package kidridicarus.agency.space;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyIndex.DrawObjectIter;

/*
 * Contains an agency with stuff that moves around on a tiled map.
 * Easily get stuff that needs to be rendered (e.g. visible agents and map layers). 
 */
public class PlatformSpace implements Disposable {
	private Agency agency;
	private TiledMap tiledMap;

	public PlatformSpace(Agency agency, TextureAtlas atlas, SpaceTemplate spaceTemp) {
		this.agency = agency;
		loadSpaceTemplate(spaceTemp, atlas);
	}

	private void loadSpaceTemplate(SpaceTemplate spaceTemp, TextureAtlas atlas) {
		tiledMap = spaceTemp.getMap();
		// give the draw layers to the agency to do whatever it wants with them
		agency.setDrawLayers(spaceTemp.getDrawLayers());
		// set the texture atlas (for sprites, etc.)
		agency.setAtlas(atlas);
		// create collision geometry from the layers marked solid in the map
		agency.createCollisionMap(spaceTemp.getSolidLayers());
		// create agents from the agent data in the map 
		agency.createAgents(spaceTemp.getAgentProps());
		// process the agent change queue after creating agents, so that player spawnpoints can be found
		agency.processChangeQ();
	}

	public TiledMap getTiledMap() {
		return tiledMap;
	}

	public World getWorld() {
		return agency.getWorld();
	}

	public void iterateThroughDrawObjects(DrawObjectIter objIter) {
		agency.iterateThroughDrawObjects(objIter);
	}

	@Override
	public void dispose() {
		tiledMap.dispose();
	}
}
