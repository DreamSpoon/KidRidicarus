package kidridicarus.agencydirector;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyEventListener;
import kidridicarus.agencydirector.space.SMBSpace;
import kidridicarus.agencydirector.space.SpaceTemplate;
import kidridicarus.agencydirector.space.SpaceTemplateLoader;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.general.GuideSpawner;
import kidridicarus.agent.general.Room;
import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;

/*
 * TODO: Guides are assigned to Agents by the AgencyDirectory.
 */
public class AgencyDirector implements Disposable {
	private Agency agency;
	private TextureAtlas atlas;
	private AssetManager manager;
	private OrthographicCamera gamecam;
	private SpaceRenderer agencyRenderer;

	private Guide smbPlayer;
	private SMBSpace smbSpace;
	private String currentRoomMusicName;
	private Music currentRoomMusic;

	public AgencyDirector(AssetManager manager, TextureAtlas atlas, OrthographicCamera gamecam) {
		this.manager = manager;
		this.atlas = atlas;
		this.gamecam = gamecam;

		agency = new Agency();
		agency.setEventListener(new AgencyEventListener() {
			@Override
			public void onPlaySound(String soundName) { playSound(soundName); }
			@Override
			public void onStartRoomMusic() { startRoomMusic(); }
			@Override
			public void onStopRoomMusic() { stopRoomMusic(); }
			@Override
			public void onStartSinglePlayMusic(String musicName) { startSinglePlayMusic(musicName); }
		});

		smbPlayer = null;

		currentRoomMusicName = "";
		currentRoomMusic = null;
	}

	public void createSpace(String spaceTemplateFilename) {
		SpaceTemplate spaceTemp = SpaceTemplateLoader.loadMap(spaceTemplateFilename);
		smbSpace = new SMBSpace(agency, atlas, spaceTemp);

		preloadRoomMusic();

		// start renderer after loading map into agency, TODO: fix this
		agencyRenderer = new SpaceRenderer(smbSpace);
	}

	private void preloadRoomMusic() {
		LinkedList<String> musicCatalog = new LinkedList<String>();
		Collection<Agent> roomList = agency.getAgentsByProperties(new String[] { KVInfo.KEY_AGENTCLASS },
				new String[] { KVInfo.VAL_ROOM });
		for(Agent rr : roomList) {
			String music = rr.getProperties().get(KVInfo.KEY_ROOMMUSIC, "", String.class);
			if(music.equals("") || musicCatalog.contains(music))
				continue;
			musicCatalog.add(music);
		}

		for(String m : musicCatalog)
			manager.load(m, Music.class);
		manager.finishLoading();
	}

	public void update(float delta) {
		// If player needs to warp, do it after the update finishes() and the draw() finishes, then apply warp
		// just before the start of the next update() call, which would be right here.
		GuideSpawner sp = smbPlayer.getAgent().getWarpSpawnpoint(); 
		if(sp != null)
			smbPlayer.getAgent().respawn(sp);

		// update agent (which includes physics and sprites) world
		smbPlayer.update(delta);
		agency.step(delta);

		// if player is not dead then use their current room to determine the gamecam position
		if(!smbPlayer.getAgent().isDead()) {
			Room inRoom = ((Mario) smbPlayer.getAgent()).getCurrentRoom();
			if(inRoom != null) {
				// set view cam position according to room
				gamecam.position.set(inRoom.getViewCenterForPos(smbPlayer.getAgent().getPosition()), 0f);
				gamecam.update();

				// check for music change
				if(currentRoomMusic == null || !inRoom.getRoommusic().equals(currentRoomMusicName))
					changeRoomMusic(inRoom.getRoommusic());
			}
		}
	}

	public void playSound(String sound) {
		manager.get(sound, Sound.class).play(AudioInfo.SOUND_VOLUME);
	}

	private void changeRoomMusic(String musicname) {
		if(currentRoomMusic != null)
			currentRoomMusic.stop();

		currentRoomMusic = manager.get(musicname, Music.class);
		currentRoomMusic.setLooping(true);
		currentRoomMusic.setVolume(AudioInfo.MUSIC_VOLUME);
		currentRoomMusic.play();

		currentRoomMusicName = musicname;
	}

	public void startRoomMusic() {
		if(currentRoomMusic != null) {
			currentRoomMusic = manager.get(currentRoomMusicName, Music.class);
			currentRoomMusic.setLooping(true);
			currentRoomMusic.setVolume(AudioInfo.MUSIC_VOLUME);
			currentRoomMusic.play();
		}
	}

	public void stopRoomMusic() {
		if(currentRoomMusic != null)
			currentRoomMusic.stop();
	}

	// play music, no loop (for things like mario powerstar)
	public void startSinglePlayMusic(String musicName) {
		Music otherMusic = manager.get(musicName, Music.class);
		otherMusic.setLooping(false);
		otherMusic.setVolume(AudioInfo.MUSIC_VOLUME);
		otherMusic.play();
	}

	/*
	 * TODO: the guide should be created from a guide def.
	 */
	public Guide createGuide() {
		if(smbPlayer != null)
			throw new IllegalStateException("Player already created. Cannot create again.");

		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> list = agency.getAgentsByProperties(
				new String[] { KVInfo.KEY_AGENTCLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNGUIDE, KVInfo.VAL_TRUE });
		if(list.isEmpty())
			smbPlayer = new Guide(agency, new Vector2(0f, 0f));
		else
			smbPlayer = new Guide(agency, list.iterator().next().getPosition());

		// TODO: create mario agent here for guide

		return smbPlayer;
	}

	public Collection<Agent>[] getAgentsToDraw() {
		return agency.getAgentsToDraw();
	}

	public World getWorld() {
		return agency.getWorld();
	}

	@Override
	public void dispose() {
		smbPlayer.dispose();
		agency.dispose();
	}

	public Agency getAgency() {
		return agency;
	}

	public void draw(SpriteBatch batch) {
		agencyRenderer.draw(batch, gamecam);
	}
}
