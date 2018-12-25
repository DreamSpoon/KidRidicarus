package kidridicarus.agencydirector.space;

import java.util.Collection;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agent.Agent;

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
		agency.setAtlas(atlas);
		agency.createCollisionMap(spaceTemp.getSolidLayers());
		agency.createAgents(spaceTemp.getAgentDefs());
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
