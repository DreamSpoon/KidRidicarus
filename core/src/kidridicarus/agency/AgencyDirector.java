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
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.space.PlatformSpace;
import kidridicarus.agency.space.SpaceRenderer;
import kidridicarus.agency.space.SpaceTemplateLoader;

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

//	private Music currentMainMusic;
//	private boolean isMainMusicPlaying;
//	private Music currentSinglePlayMusic;

	/*
	 * The soundVolume paramater is a hack, TODO put it in a better place
	 */
	public AgencyDirector(AssetManager manager, TextureAtlas atlas, AgentClassList additionalAgents,
			float soundVolume) {
		this.manager = manager;
		this.atlas = atlas;
		this.soundVolume = soundVolume;

//		currentMainMusic = null;
//		isMainMusicPlaying = false;
//		currentSinglePlayMusic = null;

		agency = new Agency(additionalAgents);
		agency.setEventListener(new AgencyEventListener() {
			@Override
			public void onPlaySound(String soundName) { playSound(soundName); }
//			@Override
//			public void onStartMusic() { startMusic(); }
//			@Override
//			public void onStopMusic() { stopMusic(); }
//			@Override
//			public void onStartSinglePlayMusic(String musicName) { startSinglePlayMusic(musicName); }
//			@Override
//			public void onChangeAndStartMusic(String musicName) { changeAndStartMusic(musicName); }
		});
	}

	public PlatformSpace createSpace(String spaceTemplateFilename) {
		if(smbSpace != null)
			throw new IllegalStateException("Space already created. Cannot create again.");

		smbSpace = new PlatformSpace(agency, atlas, SpaceTemplateLoader.loadMap(spaceTemplateFilename));
		preloadSpaceMusic();
		spaceRenderer = new SpaceRenderer();
		return smbSpace;
	}

	private void preloadSpaceMusic() {
		LinkedList<String> musicCatalog = new LinkedList<String>();
		Collection<Agent> roomList = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS },
				new String[] { AgencyKV.Room.VAL_ROOM });
		for(Agent agent : roomList) {
			String music = agent.getProperties().get(AgencyKV.Room.KEY_ROOMMUSIC, "", String.class);
			if(music.equals("") || musicCatalog.contains(music))
				continue;
			musicCatalog.add(music);
		}

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

/*	private void changeAndStartMusic(String musicname) {
		if(currentMainMusic != null)
			currentMainMusic.stop();

		currentMainMusic = manager.get(musicname, Music.class);
		startMusic();
		isMainMusicPlaying = true;
	}

	private void startMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.setLooping(true);
			currentMainMusic.setVolume(AudioInfo.MUSIC_VOLUME);
			currentMainMusic.play();
			isMainMusicPlaying = true;
		}
	}

	private void stopMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.stop();
			isMainMusicPlaying = false;
		}
	}

	// play music, no loop (for things like mario powerstar)
	private void startSinglePlayMusic(String musicName) {
		// pause the current music
		if(currentMainMusic != null)
			currentMainMusic.pause();

		// if single play music is already playing, then stop it before starting new single play music
		if(currentSinglePlayMusic != null)
			currentSinglePlayMusic.stop();

		currentSinglePlayMusic = manager.get(musicName, Music.class);
		currentSinglePlayMusic.setLooping(false);
		currentSinglePlayMusic.setVolume(AudioInfo.MUSIC_VOLUME);
		currentSinglePlayMusic.play();
		currentSinglePlayMusic.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(Music music) {
				finishSinglePlayMusic();
			}});
	}

	// resume the current music
	private void finishSinglePlayMusic() {
		if(currentMainMusic != null && isMainMusicPlaying)
			currentMainMusic.play();
	}
*/
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
		String initPlayClass = spawner.getProperties().get("playeragentclass", "", String.class);
		if(initPlayClass.equals(""))
			return null;
		return agency.createAgent(AgentDef.makePointBoundsDef(initPlayClass, spawner.getPosition()));
	}

	private Agent getMainGuideSpawn() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> list = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS, AgencyKV.Spawn.KEY_SPAWNMAIN },
				new String[] { AgencyKV.Spawn.VAL_SPAWNGUIDE, AgencyKV.VAL_TRUE });
		if(!list.isEmpty())
			return list.iterator().next();
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
