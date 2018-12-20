package kidridicarus.agencydirector.space;

import java.util.Collection;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.Agency;
import kidridicarus.agencydirector.SMBGuide;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.info.KVInfo;
import kidridicarus.tools.EncapTexAtlas;

public class SMBSpace implements Disposable {
	private Agency agency;
	private SMBGuide smbGuide;
	private TiledMap tiledMap;
	private Collection<MapLayer>[] drawLayers;

	public SMBSpace(Agency agency, TextureAtlas atlas, SpaceTemplate spaceTemp) {
		this.agency = agency;
		smbGuide = null;
		loadSpaceTemplate(spaceTemp, atlas);
	}

	private void loadSpaceTemplate(SpaceTemplate spaceTemp, TextureAtlas atlas) {
		tiledMap = spaceTemp.getMap();
		drawLayers = spaceTemp.getDrawLayers();
		agency.setEncapTexAtlas(new EncapTexAtlas(atlas, tiledMap.getTileSets()));
		agency.createCollisionMap(spaceTemp.getSolidLayers());
		agency.createAgents(spaceTemp.getAgentDefs());
	}

	public SMBGuide createGuide(Batch batch, OrthographicCamera gamecam) {
		if(smbGuide != null)
			throw new IllegalStateException("Guide already created. Cannot create again.");

		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> list = agency.getAgentsByProperties(
				new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNGUIDE, KVInfo.VAL_TRUE });
		if(list.isEmpty()) {
			smbGuide = new SMBGuide(agency,
					(Mario) agency.createAgent(ADefFactory.makeMarioDef(new Vector2(0f, 0f))), batch, gamecam);
		}
		else {
			smbGuide = new SMBGuide(agency,
					(Mario) agency.createAgent(ADefFactory.makeMarioDef(list.iterator().next().getPosition())), batch, gamecam);
		}

		return smbGuide;
	}

	public void update(float delta) {
		smbGuide.preUpdate(delta);

		// update agent (which includes physics and sprites) world
		agency.update(delta);
		
		smbGuide.postUpdate(delta);
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

	public boolean isGameOver() {
		return smbGuide.isGameOver();
	}

	public boolean isGameWon() {
		return smbGuide.isGameWon();
	}

	@Override
	public void dispose() {
		smbGuide.dispose();
	}
}
