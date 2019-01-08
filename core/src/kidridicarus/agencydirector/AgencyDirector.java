package kidridicarus.agencydirector;

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

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyEventListener;
import kidridicarus.agency.AgentDef;
import kidridicarus.agencydirector.space.PlatformSpace;
import kidridicarus.agencydirector.space.SpaceRenderer;
import kidridicarus.agencydirector.space.SpaceTemplateLoader;
import kidridicarus.agent.Agent;
import kidridicarus.guide.MainGuide;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.info.PowerupInfo.PowChar;

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
			smbGuide.setAdviseAgent(agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.VAL_MARIO, startPos)));
		else if(pc == PowChar.SAMUS)
			smbGuide.setAdviseAgent(agency.createAgent(AgentDef.makePointBoundsDef(KVInfo.VAL_SAMUS, startPos)));

		return smbGuide;
	}

	private Vector2 getMainSpawnPosition() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> list = agency.getAgentsByProperties(
				new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNGUIDE, KVInfo.VAL_TRUE });
		if(!list.isEmpty())
			return list.iterator().next().getPosition();
		else
			return new Vector2(0f, 0f);
	}

	private void preloadSpaceMusic() {
		LinkedList<String> musicCatalog = new LinkedList<String>();
		Collection<Agent> roomList = agency.getAgentsByProperties(new String[] { KVInfo.KEY_AGENTCLASS },
				new String[] { KVInfo.VAL_ROOM });
		for(Agent agent : roomList) {
			String music = agent.getProperties().get(KVInfo.KEY_ROOMMUSIC, "", String.class);
			if(music.equals("") || musicCatalog.contains(music))
				continue;
			musicCatalog.add(music);
		}

		for(String m : musicCatalog)
			manager.load(m, Music.class);
		manager.finishLoading();
	}

	public void update(float delta) {
		smbGuide.preUpdate(delta);
		agency.update(delta);
		smbGuide.postUpdate();
	}

	public void playSound(String sound) {
		manager.get(sound, Sound.class).play(AudioInfo.SOUND_VOLUME);
	}

	private void changeAndStartMusic(String musicname) {
		if(currentMainMusic != null)
			currentMainMusic.stop();

		currentMainMusic = manager.get(musicname, Music.class);
		startMusic();
		isMainMusicPlaying = true;
	}

	public void startMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.setLooping(true);
			currentMainMusic.setVolume(AudioInfo.MUSIC_VOLUME);
			currentMainMusic.play();
			isMainMusicPlaying = true;
		}
	}

	public void stopMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.stop();
			isMainMusicPlaying = false;
		}
	}

	// play music, no loop (for things like mario powerstar)
	public void startSinglePlayMusic(String musicName) {
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

	public Collection<Agent>[] getAgentsToDraw() {
		return agency.getAgentsToDraw();
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
		smbGuide.dispose();
		smbSpace.dispose();
		agency.dispose();
	}
}
