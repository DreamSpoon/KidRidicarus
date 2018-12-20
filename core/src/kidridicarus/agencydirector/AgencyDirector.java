package kidridicarus.agencydirector;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyEventListener;
import kidridicarus.agencydirector.space.SMBSpace;
import kidridicarus.agencydirector.space.SpaceRenderer;
import kidridicarus.agencydirector.space.SpaceTemplateLoader;
import kidridicarus.agent.Agent;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;

public class AgencyDirector implements Disposable {
	private AssetManager manager;
	private TextureAtlas atlas;
	private SMBSpace smbSpace;
	private SpaceRenderer spaceRenderer;
	private Agency agency;

	private Music currentMusic;

	public AgencyDirector(AssetManager manager, TextureAtlas atlas) {
		this.manager = manager;
		this.atlas = atlas;

		currentMusic = null;

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

	public SMBSpace createSpace(String spaceTemplateFilename) {
		smbSpace = new SMBSpace(agency, atlas, SpaceTemplateLoader.loadMap(spaceTemplateFilename));
		preloadSpaceMusic();
		spaceRenderer = new SpaceRenderer();
		return smbSpace;
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
		smbSpace.update(delta);
	}

	public void playSound(String sound) {
		manager.get(sound, Sound.class).play(AudioInfo.SOUND_VOLUME);
	}

	private void changeAndStartMusic(String musicname) {
		if(currentMusic != null)
			currentMusic.stop();

		currentMusic = manager.get(musicname, Music.class);
		startMusic();
	}

	public void startMusic() {
		if(currentMusic != null) {
			currentMusic.setLooping(true);
			currentMusic.setVolume(AudioInfo.MUSIC_VOLUME);
			currentMusic.play();
		}
	}

	public void stopMusic() {
		if(currentMusic != null)
			currentMusic.stop();
	}

	// play music, no loop (for things like mario powerstar)
	public void startSinglePlayMusic(String musicName) {
		Music otherMusic = manager.get(musicName, Music.class);
		otherMusic.setLooping(false);
		otherMusic.setVolume(AudioInfo.MUSIC_VOLUME);
		otherMusic.play();
	}

	public Collection<Agent>[] getAgentsToDraw() {
		return agency.getAgentsToDraw();
	}

	public void draw(SMBGuide guide) {
		spaceRenderer.draw(smbSpace, guide);
	}

	@Override
	public void dispose() {
		smbSpace.dispose();
		agency.dispose();
	}
}
