package kidridicarus.agencydirector.space;

import java.util.Collection;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agent.Agent;

/*
 * Contains an agency with stuff that moves around on a tiled map.
 * Easily get stuff that needs to be rendered (e.g. visible agents and map layers). 
 */
public class PlatformSpace implements Disposable {
	private Agency agency;
	private TiledMap tiledMap;
	private Collection<MapLayer>[] drawLayers;

	public PlatformSpace(Agency agency, TextureAtlas atlas, SpaceTemplate spaceTemp) {
		this.agency = agency;
		loadSpaceTemplate(spaceTemp, atlas);
	}

	private void loadSpaceTemplate(SpaceTemplate spaceTemp, TextureAtlas atlas) {
		tiledMap = spaceTemp.getMap();
		drawLayers = spaceTemp.getDrawLayers();
		// set the texture atlas (for sprites, etc.)
		agency.setAtlas(atlas);
		// create collision geometry from the layers marked solid in the map
		agency.createCollisionMap(spaceTemp.getSolidLayers());
		// create agents from the agent data in the map 
		agency.createAgents(spaceTemp.getAgentDefs());
		// process the agent change queue after creating agents, so that guide (player) spawnpoints can be found
		agency.processAgentChangeQ();
	}

	public TiledMap getTiledMap() {
		return tiledMap;
	}

	public Collection<MapLayer>[] getDrawLayers() {
		return drawLayers;
	}

	public World getWorld() {
		return agency.getWorld();
	}

	public Collection<Agent>[] getAgentsToDraw() {
		return agency.getAgentsToDraw();
	}

	@Override
	public void dispose() {
		tiledMap.dispose();
	}
}
