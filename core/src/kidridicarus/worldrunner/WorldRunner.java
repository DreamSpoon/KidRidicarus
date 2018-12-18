package kidridicarus.worldrunner;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.info.AudioInfo;
import kidridicarus.info.KVInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.player.MarioRole;
import kidridicarus.roles.robot.general.PlayerSpawner;
import kidridicarus.roles.robot.general.Room;
import kidridicarus.tools.EncapTexAtlas;
import kidridicarus.worldrunner.maploader.GamemapLoader;
import kidridicarus.worldrunner.maploader.KidRidLevel;

public class WorldRunner implements Disposable {
	private TextureAtlas atlas;
	private AssetManager manager;
	private TiledMap map;
	private RoleWorld subWR;
	private RoleEventListener subEventListener;
	private Collection<MapLayer>[] drawLayers;

	private Player player;
	private String currentRoomMusicName;
	private Music currentRoomMusic;

	private OrthographicCamera gamecam;

	private class MyRoleEventListener implements RoleEventListener {
		@Override
		public void onPlaySound(String soundName) {
			playSound(soundName);
		}

		@Override
		public void onStartRoomMusic() {
			startRoomMusic();
		}

		@Override
		public void onStopRoomMusic() {
			stopRoomMusic();
		}

		@Override
		public void onStartSinglePlayMusic(String musicName) {
			startSinglePlayMusic(musicName);
		}
	}

	public WorldRunner(AssetManager manager, TextureAtlas atlas, OrthographicCamera gamecam) {
		subWR = new RoleWorld();
		this.subEventListener = new MyRoleEventListener();
		subWR.setRoleEventListener(subEventListener);

		this.manager = manager;
		this.atlas = atlas;
		this.gamecam = gamecam;

		player = null;

		currentRoomMusicName = "";
		currentRoomMusic = null;
		drawLayers = null;
		map = null;
	}

	public void loadMap(String filename) {
		KidRidLevel gamemapStuff = GamemapLoader.loadMap(filename);
		map = gamemapStuff.getMap();
		subWR.setEncapTexAtlas(new EncapTexAtlas(atlas, map.getTileSets()));

		subWR.createCollisionMap(gamemapStuff.getSolidLayers());
		drawLayers = gamemapStuff.getDrawLayers();
		subWR.createRobots(gamemapStuff.getRobotDefs());

		preloadRoomMusic();
	}

	public TiledMap getMap() {
		return map;
	}

	public Collection<MapLayer>[] getDrawLayers() {
		return drawLayers;
	}

	private void preloadRoomMusic() {
		LinkedList<String> musicCatalog = new LinkedList<String>();
		Collection<RobotRole> roomList = subWR.getRobotsByProperties(new String[] { KVInfo.KEY_ROBOTROLECLASS },
				new String[] { KVInfo.VAL_ROOM });
		for(RobotRole rr : roomList) {
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
		PlayerSpawner sp = player.getRole().getWarpSpawnpoint(); 
		if(sp != null)
			player.getRole().respawn(sp);

		// update role (which includes physics and sprites) world
		subWR.step(delta, player.getBI());

		// if player is not dead then use their current room to determine the gamecam position
		if(!player.getRole().isDead()) {
			Room inRoom = ((MarioRole) player.getRole()).getCurrentRoom();
			if(inRoom != null) {
				// set view cam position according to room
				gamecam.position.set(inRoom.getViewCenterForPos(player.getRole().getPosition()), 0f);
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

	public Player createPlayer() {
		if(player != null)
			throw new IllegalStateException("Player already created. Cannot create again.");

		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<RobotRole> list = subWR.getRobotsByProperties(
				new String[] { KVInfo.KEY_ROBOTROLECLASS, KVInfo.KEY_SPAWNMAIN },
				new String[] { KVInfo.VAL_SPAWNPLAYER, KVInfo.VAL_TRUE });
		if(list.isEmpty())
			player = new Player(subWR, new Vector2(0f, 0f));
		else
			player = new Player(subWR, list.iterator().next().getPosition());
			
		return player;
	}

	public Player getPlayer() {
		return player;
	}

	public Collection<RobotRole>[] getRobotsToDraw() {
		return subWR.getRobotsToDraw();
	}

	public World getWorld() {
		return subWR.getWorld();
	}

	@Override
	public void dispose() {
		player.dispose();
		subWR.dispose();
	}

	public RoleWorld getSubWR() {
		return subWR;
	}
}
