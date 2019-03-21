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
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.AllowOrderList.AllowOrderListIter;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.TiledMapMetaAgent;
import kidridicarus.game.tool.QQ;

/*
 * Run the agency, insert players into the agency, and take direction from the Agency to play sounds, music, etc.
 * Why is AgencyDirectory necessary and what is the difference between Agency and AgencyDirector?
 * Basically:
 *   -AgencyDirector can load resources from files
 *   -Agency cannot load resources from files
 * Other concepts flow from this general concept, i.e.
 *   AgencyDirector can load map files, load sound and music files referenced in the map files, etc., and then
 *   create Agents that use the resources in these files. AgencyDirector manages resources/files.
 *   Agency creates and manages Agents.
 *   Agents do not manage the resources that they need - they only request use of resources.
 *   i.e. by requesting use of preloaded music/image/resource files at Agent constructor time,
 *   and releasing use of these resources at Agent disposal time.
 *   AgencyDirector can alos perform some garbage collection functions, e.g. by unloading level music files
 *   when a level ends, so that memory usage is minimized.
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
	public AgencyDirector(AssetManager manager, Batch batch, TextureAtlas atlas, AgentClassList additionalAgents,
			float soundVolume) {
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
		agency.iterateThroughDrawListeners(new AllowOrderListIter() {
			@Override
			public boolean iterate(Object obj) {
				if(obj instanceof AgentDrawListener)
					((AgentDrawListener) obj).draw(adBatch);
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

	public Agent createInitialPlayerAgent(ObjectProperties playerAgentProperties) {
		// find main player spawner
		Agent spawner = getMainPlayerSpawner();
		if(spawner == null)
			return null;
		// if no agent properties given then use spawner to determine player class and position
		if(playerAgentProperties == null) {
			String initPlayClass = spawner.getProperty("playeragentclass", null, String.class);
			if(initPlayClass == null)
				return null;
			return agency.createAgent(Agent.createPointAP(initPlayClass, spawner.getPosition()));
		}
		// otherwise use agent properties and set start point to main spawn point
		else {
			playerAgentProperties.put(AgencyKV.Spawn.KEY_START_POINT, spawner.getPosition());
			return agency.createAgent(playerAgentProperties);
		}
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

	public void disposeAndRemoveAllAgents() {
		agency.disposeAndRemoveAllAgents();
	}

	@Override
	public void dispose() {
		agency.dispose();
		tiledMapRenderer.dispose();
	}
}
