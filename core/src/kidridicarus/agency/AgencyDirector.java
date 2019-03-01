package kidridicarus.agency;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.space.PlatformSpace;
import kidridicarus.agency.space.SpaceRenderer;
import kidridicarus.agency.space.SpaceTemplateLoader;
import kidridicarus.agency.tool.DrawOrderAlias;

/*
 * Run the agency, insert guides (players) into the agency, and take direction from the agency to
 * play sounds, music, etc. 
 */
public class AgencyDirector implements Disposable {
	private AssetManager manager;
	private TextureAtlas atlas;
	private PlatformSpace smbSpace;
	private SpaceRenderer spaceRenderer;
	private Agency agency;
	private float soundVolume;
	private DrawOrderAlias[] drawOrderAliasList;

	/*
	 * The soundVolume paramater is a hack, TODO put it in a better place
	 */
	public AgencyDirector(AssetManager manager, TextureAtlas atlas, DrawOrderAlias[] drawOrderAliasList,
			AgentClassList additionalAgents, float soundVolume) {
		this.manager = manager;
		this.atlas = atlas;
		this.drawOrderAliasList = drawOrderAliasList;
		this.soundVolume = soundVolume;

		agency = new Agency(additionalAgents);
		agency.setEventListener(new AgencyEventListener() {
			@Override
			public void onPlaySound(String soundName) { playSound(soundName); }
		});
	}

	public PlatformSpace createSpace(String spaceTemplateFilename) {
		if(smbSpace != null)
			throw new IllegalStateException("Space already created. Cannot create again.");

		smbSpace = new PlatformSpace(agency, atlas, SpaceTemplateLoader.loadMap(
				spaceTemplateFilename, drawOrderAliasList));
		preloadSpaceMusic();
		spaceRenderer = new SpaceRenderer();
		return smbSpace;
	}

	private void preloadSpaceMusic() {
		// create a catlog of unique musics
		LinkedList<String> musicCatalog = new LinkedList<String>();
		Collection<Agent> roomList = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS },
				new String[] { AgencyKV.Room.VAL_ROOM });
		for(Agent agent : roomList) {
			String music = agent.getProperty(AgencyKV.Room.KEY_ROOMMUSIC, "", String.class);
			if(music.equals("") || musicCatalog.contains(music))
				continue;
			musicCatalog.add(music);
		}

		// load the musics
		for(String m : musicCatalog)
			manager.load(m, Music.class);
		manager.finishLoading();
	}

	public void update(float delta) {
		agency.update(delta);
	}

	private void playSound(String sound) {
		manager.get(sound, Sound.class).play(soundVolume);
	}

	public void draw(final Batch batch, OrthographicCamera camera) {
		spaceRenderer.draw(smbSpace, batch, camera);
	}

	public Agency getAgency() {
		return agency;
	}

	public Agent createInitialPlayerAgent() {
		Agent spawner = getMainGuideSpawn();
		if(spawner == null)
			return null;
		String initPlayClass = spawner.getProperty("playeragentclass", null, String.class);
		if(initPlayClass == null)
			return null;
		return agency.createAgent(Agent.createPointAP(initPlayClass, spawner.getPosition()));
	}

	private Agent getMainGuideSpawn() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> spawnList = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS, AgencyKV.Spawn.KEY_SPAWNMAIN },
				new String[] { AgencyKV.Spawn.VAL_SPAWNGUIDE, AgencyKV.VAL_TRUE });
		if(!spawnList.isEmpty())
			return spawnList.iterator().next();
		else
			return null;
	}

	@Override
	public void dispose() {
		if(spaceRenderer != null)
			spaceRenderer.dispose();
		if(smbSpace != null)
			smbSpace.dispose();
		if(agency != null)
			agency.dispose();
	}
}
