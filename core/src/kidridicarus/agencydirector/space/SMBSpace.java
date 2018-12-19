package kidridicarus.agencydirector.space;

import java.util.Collection;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.Agency;
import kidridicarus.agent.Agent;
import kidridicarus.tools.EncapTexAtlas;

public class SMBSpace {
	private Agency agency;
	private TiledMap tiledMap;
	private Collection<MapLayer>[] drawLayers;

	public SMBSpace(Agency agency, TextureAtlas atlas, SpaceTemplate spaceTemp) {
		this.agency = agency;
		drawLayers = null;
		tiledMap = null;

		loadSpaceTemplate(spaceTemp, atlas);
	}

	public void update(float delta) {
	}

	private void loadSpaceTemplate(SpaceTemplate spaceTemp, TextureAtlas atlas) {
		tiledMap = spaceTemp.getMap();
		agency.setEncapTexAtlas(new EncapTexAtlas(atlas, tiledMap.getTileSets()));

		agency.createCollisionMap(spaceTemp.getSolidLayers());
		drawLayers = spaceTemp.getDrawLayers();
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
}
