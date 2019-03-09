package kidridicarus.common.agencydirector;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyEventListener;
import kidridicarus.agency.AgentClassList;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;
import kidridicarus.common.agent.collisionmap.TiledMapMetaAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.DrawOrderAlias;
import kidridicarus.game.tool.QQ;

/*
 * Run the agency, insert players into the agency, and take direction from the agency to play sounds, music, etc. 
 */
public class AgencyDirector implements Disposable {
	private AssetManager manager;
	private Agency agency;
	private float soundVolume;
	private AgencyDrawBatch adBatch;
	private OrthogonalTiledMapRenderer tiledMapRenderer;
	private LinkedList<String> musicCatalog;

	/*
	 * The soundVolume paramater is a hack, TODO put it in a better place
	 */
	public AgencyDirector(AssetManager manager, Batch batch, TextureAtlas atlas, DrawOrderAlias[] drawOrderAliasList,
			AgentClassList additionalAgents, float soundVolume) {
		this.manager = manager;
		tiledMapRenderer = new OrthogonalTiledMapRenderer(null, UInfo.P2M(1f), batch);
		adBatch = new AgencyDrawBatch(batch, tiledMapRenderer);
		musicCatalog = new LinkedList<String>();
		this.soundVolume = soundVolume;

		agency = new Agency(additionalAgents);
		agency.setEventListener(new AgencyEventListener() {
			@Override
			public void onPlaySound(String soundName) { playSound(soundName); }
			@Override
			public void onRegisterMusic(String musicName) { registerMusic(musicName); }
		});
		agency.setAtlas(atlas);
	}

	public void update(float delta) {
		agency.update(delta);
	}

	public void draw(OrthographicCamera camera) {
		adBatch.setView(camera);
		adBatch.begin();
		agency.iterateThroughDrawObjects(new AllowOrderListIter() {
			@Override
			public boolean iterate(Object obj) {
				if(obj instanceof DrawableAgent)
					((DrawableAgent) obj).draw(adBatch);
				else
					QQ.pr("unknown object in draw list iteration: " + obj);
				// return false to continue iterating
				return false;
			}
		});
		adBatch.end();
	}

	private void playSound(String soundName) {
		manager.get(soundName, Sound.class).play(soundVolume);
	}

	private void registerMusic(String musicName) {
		if(musicName.equals("") || musicCatalog.contains(musicName))
			return;
		musicCatalog.add(musicName);
		manager.load(musicName, Music.class);
		manager.finishLoading();
	}

	public void createMapAgent(String levelFilename) {
		// load map from file
		TiledMap tiledMap = (new TmxMapLoader()).load(levelFilename);
		// create the agent
		agency.createAgent(TiledMapMetaAgent.makeAP(tiledMap));
	}

	public Agent createInitialPlayerAgent() {
		Agent spawner = getMainPlayerSpawner();
		if(spawner == null)
			return null;
		String initPlayClass = spawner.getProperty("playeragentclass", null, String.class);
		if(initPlayClass == null)
			return null;
		return agency.createAgent(Agent.createPointAP(initPlayClass, spawner.getPosition()));
	}

	private Agent getMainPlayerSpawner() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> spawnList = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS, CommonKV.Spawn.KEY_SPAWNMAIN },
				new String[] { CommonKV.AgentClassAlias.VAL_PLAYERSPAWNER, CommonKV.VAL_TRUE });
		if(!spawnList.isEmpty())
			return spawnList.iterator().next();
		else
			return null;
	}

	public Agency getAgency() {
		return agency;
	}

	@Override
	public void dispose() {
		agency.dispose();
		tiledMapRenderer.dispose();
	}
}
