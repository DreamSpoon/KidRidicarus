package kidridicarus.agency;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.guide.MainGuide;
import kidridicarus.agency.space.PlatformSpace;
import kidridicarus.agency.space.SpaceRenderer;
import kidridicarus.agency.space.SpaceTemplateLoader;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.KVInfo;
import kidridicarus.game.info.PowerupInfo.PowChar;

/*
 * Run the agency, insert guides (players) into the agency, and take direction from the agency to
 * play sounds, music, etc. 
 */
public class AgencyDirector implements Disposable {
	private AssetManager manager;
	private TextureAtlas atlas;
	private PlatformSpace smbSpace;
	private MainGuide smbGuide;
	private SpaceRenderer spaceRenderer;
	private Agency agency;

	private Music currentMainMusic;
	private boolean isMainMusicPlaying;
	private Music currentSinglePlayMusic;

	public AgencyDirector(AssetManager manager, TextureAtlas atlas) {
		this.manager = manager;
		this.atlas = atlas;
		smbGuide = null;

		currentMainMusic = null;
		isMainMusicPlaying = false;
		currentSinglePlayMusic = null;

		agency = new Agency();
		agency.setEventListener(new AgencyEventListener() {
			@Override
			public void onPlaySound(String soundName) { playSound(soundName); }
			@Override
			public void onStartMusic() { startMusic(); }
			@Override
			public void onStopMusic() { stopMusic(); }
			@Override
			public void onStartSinglePlayMusic(String musicName) { startSinglePlayMusic(musicName); }
			@Override
			public void onChangeAndStartMusic(String musicName) { changeAndStartMusic(musicName); }
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

	public MainGuide createGuide(Batch batch, OrthographicCamera gamecam, PowChar pc) {
		if(smbGuide != null)
			throw new IllegalStateException("Guide already created. Cannot create again.");

		Vector2 startPos = getMainSpawnPosition();
		smbGuide = new MainGuide(agency, batch, gamecam);
		if(pc == PowChar.MARIO)
			smbGuide.setAdviseAgent(agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.SMB.VAL_MARIO, startPos)));
		else if(pc == PowChar.SAMUS)
			smbGuide.setAdviseAgent(agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.Metroid.VAL_SAMUS, startPos)));

		return smbGuide;
	}

	private Vector2 getMainSpawnPosition() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> list = agency.getAgentsByProperties(
				new String[] { KVInfo.Spawn.KEY_AGENTCLASS, KVInfo.Spawn.KEY_SPAWNMAIN },
				new String[] { KVInfo.Spawn.VAL_SPAWNGUIDE, KVInfo.VAL_TRUE });
		if(!list.isEmpty())
			return list.iterator().next().getPosition();
		else
			return new Vector2(0f, 0f);
	}

	private void preloadSpaceMusic() {
		LinkedList<String> musicCatalog = new LinkedList<String>();
		Collection<Agent> roomList = agency.getAgentsByProperties(new String[] { KVInfo.Spawn.KEY_AGENTCLASS },
				new String[] { KVInfo.Room.VAL_ROOM });
		for(Agent agent : roomList) {
			String music = agent.getProperties().get(KVInfo.Room.KEY_ROOMMUSIC, "", String.class);
			if(music.equals("") || musicCatalog.contains(music))
				continue;
			musicCatalog.add(music);
		}

		for(String m : musicCatalog)
			manager.load(m, Music.class);
		manager.finishLoading();
	}

	public void update(float delta) {
		smbGuide.preUpdate();
		agency.update(delta);
		smbGuide.postUpdate();
	}

	private void playSound(String sound) {
		manager.get(sound, Sound.class).play(AudioInfo.SOUND_VOLUME);
	}

	private void changeAndStartMusic(String musicname) {
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

	public void draw(MainGuide guide) {
		spaceRenderer.draw(smbSpace, guide);
	}

	public boolean isGameOver() {
		return smbGuide.isGameOver();
	}

	public boolean isGameWon() {
		return smbGuide.isGameWon();
	}

	@Override
	public void dispose() {
		if(spaceRenderer != null)
			spaceRenderer.dispose();
		if(smbGuide != null)
			smbGuide.dispose();
		if(smbSpace != null)
			smbSpace.dispose();
		if(agency != null)
			agency.dispose();
	}
}
