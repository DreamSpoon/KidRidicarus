package kidridicarus.game.guide;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agencydirector.AgencyDirector;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agent.scrollbox.ScrollBox;
import kidridicarus.common.agent.scrollkillbox.ScrollKillBox;
import kidridicarus.common.agent.scrollpushbox.ScrollPushBox;
import kidridicarus.common.info.AudioInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.KeyboardMapping;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.common.tool.QQ;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.info.MetroidKV;
import kidridicarus.game.info.SMB1_KV;
import kidridicarus.game.powerup.KidIcarusPow;
import kidridicarus.game.powerup.MetroidPow;
import kidridicarus.game.powerup.SMB1_Pow;

/*
 * Guide AKA Player (not actually tho - eventually this class will split into Player and others).
 * Handles:
 *   -user input
 *   -camera
 *   -music changes
 *   -drawing HUDs
 *   -returning info about state of play.
 */
public class Guide implements Disposable {
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 20);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_Y * 15);
	private static final float KEEP_ALIVE_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 22);
	private static final float KEEP_ALIVE_HEIGHT = UInfo.P2M(UInfo.TILEPIX_Y * 15);
	private static final Vector2 SAFETY_RESPAWN_OFFSET = UInfo.P2MVector(0f, 8f);

	private AssetManager manager;
	private Stage stageHUD;
	private Agency agency;
	private MoveAdvice inputMoveAdvice;
	private PlayerAgent playerAgent;
	private AgentSpawnTrigger spawnTrigger;
	private KeepAliveBox keepAliveBox;
	private ScrollBox scrollBox;

	private String currentMainMusicName;
	private Music currentMainMusic;
	private boolean isMainMusicPlaying;
	private Music currentSinglePlayMusic;
	private Vector2 lastViewCenter;

	// TODO This is hack - remove! -used for registerMusic when rooms are spawned (rooms register their music
	//   via Agency's Ear).
	private AgencyDirector director;

	public Guide(AgencyDirector director, Agency agency, AssetManager manager, Stage stageHUD) {
		this.director = director;
		this.manager = manager;
		this.stageHUD = stageHUD;
		this.agency = agency;

		inputMoveAdvice = new MoveAdvice();
		playerAgent = null;
		spawnTrigger = null;
		keepAliveBox = null;
		scrollBox = null;
		currentMainMusicName = "";
		currentMainMusic = null;
		isMainMusicPlaying = false;
		currentSinglePlayMusic = null;
		lastViewCenter = new Vector2(0f, 0f);
	}

	private void switchHUDtoNewPlayerAgent() {
		stageHUD.clear();
		playerAgent.getSupervisor().setStageHUD(stageHUD);
	}

	private void handleInput() {
		inputMoveAdvice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		inputMoveAdvice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		inputMoveAdvice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		inputMoveAdvice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		inputMoveAdvice.action0 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);
		inputMoveAdvice.action1 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_MARIO))
			Powerup.tryPushPowerup(playerAgent, new SMB1_Pow.FireFlowerPow());
		else if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_SAMUS))
			Powerup.tryPushPowerup(playerAgent, new MetroidPow.EnergyPow());
		else if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP_PIT))
			Powerup.tryPushPowerup(playerAgent, new KidIcarusPow.HeartsPow(1));
	}

	public void preUpdateAgency(float delta) {
		// get user input
		handleInput();

		// ensure spawn trigger and keep alive box follow view center
		spawnTrigger.setTarget(getViewCenter());
		keepAliveBox.setTarget(getViewCenter());
		if(scrollBox != null)
			scrollBox.setTarget(getViewCenter());
		// pass user input to player agent's supervisor
		playerAgent.getSupervisor().setMoveAdvice(inputMoveAdvice);
		playerAgent.getSupervisor().preUpdateAgency(delta);
	}

	public void updateAgency() {
		// check for "out-of-character" powerup received and change to appropriate character for powerup
		Powerup nonCharPowerup = playerAgent.getSupervisor().getNonCharPowerups().getFirst();
		playerAgent.getSupervisor().clearNonCharPowerups();
		if(nonCharPowerup != null)
			switchAgentType(nonCharPowerup.getPowerupCharacter());
	}

	public void postUpdateAgency() {
		playerAgent.getSupervisor().postUpdateAgency();
		checkCreateScrollBox();
	}

	/*
	 * As the player moves into and out of rooms, the scroll box may need to be created / removed / changed.
	 */
	private void checkCreateScrollBox() {
		RoomBox currentRoom = playerAgent.getCurrentRoom();
		if(currentRoom == null)
			return;

		Direction4 scrollDir = Direction4.fromString(
				currentRoom.getProperty(CommonKV.Room.KEY_SCROLL_DIR, "", String.class));
		// if current room has scroll push box property = true then create/change to scroll push box
		if(currentRoom.getProperty(CommonKV.Room.KEY_SCROLL_PUSHBOX, false, Boolean.class)) {
			if(scrollBox != null && !(scrollBox instanceof ScrollPushBox)) {
				agency.removeAgent(scrollBox);
				scrollBox.dispose();
				scrollBox = null;
			}
			// if scroll box needs to be created and a valid scroll direction is given then create push box
			if(scrollBox == null && scrollDir != Direction4.NONE)
				scrollBox = (ScrollPushBox) agency.createAgent(ScrollPushBox.makeAP(getViewCenter(), scrollDir));
		}
		// if current room has scroll kill box property = true then create/change to scroll kill box
		else if(currentRoom.getProperty(CommonKV.Room.KEY_SCROLL_KILLBOX, false, Boolean.class)) {
			if(scrollBox != null && !(scrollBox instanceof ScrollKillBox)) {
				agency.removeAgent(scrollBox);
				scrollBox.dispose();
				scrollBox = null;
			}
			// if scroll box needs to be created and a valid scroll direction is given then create kill box
			if(scrollBox == null && scrollDir != Direction4.NONE)
				scrollBox = (ScrollKillBox) agency.createAgent(ScrollKillBox.makeAP(getViewCenter(), scrollDir));
		}
		// need to remove a scroll box?
		else if(scrollBox != null) {
			agency.removeAgent(scrollBox);
			scrollBox.dispose();
			scrollBox = null;
		}
	}

	private void switchAgentType(PowChar pc) {
		Vector2 currentPos = new Vector2(0f, 0f);
		boolean facingRight = false;
		if(playerAgent != null) {
			currentPos = playerAgent.getPosition().add(SAFETY_RESPAWN_OFFSET);
			facingRight = playerAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE,
					Direction4.class) == Direction4.RIGHT;
			agency.removeAgent(playerAgent);
			playerAgent.dispose();
			playerAgent = null;
		}

		switch(pc) {
			default:
			case MARIO:
				doAgentMake(SMB1_KV.AgentClassAlias.VAL_MARIO, currentPos, facingRight);
				break;
			case PIT:
				doAgentMake(KidIcarusKV.AgentClassAlias.VAL_PIT, currentPos, facingRight);
				break;
			case SAMUS:
				doAgentMake(MetroidKV.AgentClassAlias.VAL_SAMUS, currentPos, facingRight);
				break;
			case NONE:
				break;
		}
	}

	private void doAgentMake(String classAlias, Vector2 position, boolean facingRight) {
		ObjectProperties props = Agent.createPointAP(classAlias, position);
		if(facingRight)
			props.put(CommonKV.KEY_DIRECTION, Direction4.RIGHT);
		playerAgent = (PlayerAgent) agency.createAgent(props);
		switchHUDtoNewPlayerAgent();
	}

	public void updateCamera(OrthographicCamera gamecam) {
		// if player is not dead then use their current room to determine the gamecam position
		if(!playerAgent.getSupervisor().isGameOver()) {
			gamecam.position.set(getViewCenter(), 0f);
			gamecam.update();
		}
	}

	// draw the player HUD after the game world has been rendered
	public void postRenderFrame() {
		playerAgent.getSupervisor().drawHUD();
	}

	public boolean isGameWon() {
		return playerAgent.getSupervisor().isAtLevelEnd();
	}

	public boolean isGameOver() {
		return playerAgent.getSupervisor().isGameOver();
	}

	// create an Ear to give to Agency, so that Guide can receive sound/music callbacks from Agency
	public Ear createEar() {
		return new Ear() {
			@Override
			public void registerMusic(String musicName) { doRegisterMusic(musicName); }
			@Override
			public void playSound(String soundName) { doPlaySound(soundName); }
			@Override
			public void changeAndStartMainMusic(String musicName) { doChangeAndStartMainMusic(musicName); }
			@Override
			public void startSinglePlayMusic(String musicName) { doStartSinglePlayMusic(musicName); }
			@Override
			public void stopAllMusic() { doStopAllMusic(); }
		};
	}

	private void doPlaySound(String soundName) {
		manager.get(soundName, Sound.class).play(AudioInfo.SOUND_VOLUME);
	}

	// TODO This is a hack - registerMusic should be done before loading map file - read map file init agents
	// for rooms, then harvest music names.
	private void doRegisterMusic(String musicName) {
		director.registerMusic(musicName);
	}

	private void doChangeAndStartMainMusic(String musicName) {
		// exit if no name given or if already playing given music
		if(musicName == null || currentMainMusicName.equals(musicName))
			return;

		// stop main music if it is playing
		if(currentMainMusic != null)
			currentMainMusic.stop();

		currentMainMusic = manager.get(musicName, Music.class);
		startMainMusic();
		currentMainMusicName = musicName;
	}

	private void startMainMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.setLooping(true);
			currentMainMusic.setVolume(AudioInfo.MUSIC_VOLUME);
			currentMainMusic.play();
			isMainMusicPlaying = true;
		}
	}

	private void doStopMainMusic() {
		if(currentMainMusic != null) {
			currentMainMusic.stop();
			isMainMusicPlaying = false;
		}
	}

	// play music, no loop (for things like mario powerstar)
	private void doStartSinglePlayMusic(String musicName) {
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
				returnToMainMusic();
			}});
	}

	// resume the current music if it was playing
	private void returnToMainMusic() {
		if(currentMainMusic != null && isMainMusicPlaying)
			currentMainMusic.play();
	}

	private void doStopAllMusic() {
		// stop current main music if playing
		if(currentMainMusic != null)
			currentMainMusic.stop();
		currentMainMusic = null;
		// stop single play music if playing
		if(currentSinglePlayMusic != null)
			currentSinglePlayMusic.stop();
		currentSinglePlayMusic = null;
	}

	public String getNextLevelFilename() {
		return playerAgent.getSupervisor().getNextLevelFilename();
	}

	public ObjectProperties getCopyPlayerAgentProperties() {
		return playerAgent.getCopyAllProperties();
	}

	public boolean insertPlayerAgent(ObjectProperties playerAgentProperties) {
		if(playerAgent != null)
			throw new IllegalStateException("Guide can only insert one Agent (do more code;).");

		// find main player spawner and return fail if none found
		Agent spawner = getMainPlayerSpawner();
		if(spawner == null)
			return false;

		// spawn player with properties at spawn location
		playerAgent = spawnPlayerAgentWithProperties(playerAgentProperties, spawner);
		// create player's associated agents (generally, they follow player)
		spawnTrigger = (AgentSpawnTrigger) agency.createAgent(
				AgentSpawnTrigger.makeAP(getViewCenter(), SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
		spawnTrigger.setEnabled(true);
		keepAliveBox = (KeepAliveBox) agency.createAgent(
				KeepAliveBox.makeAP(getViewCenter(), KEEP_ALIVE_WIDTH, KEEP_ALIVE_HEIGHT));

		switchHUDtoNewPlayerAgent();

		return true;
	}

	private PlayerAgent spawnPlayerAgentWithProperties(ObjectProperties playerAgentProperties, Agent spawner) {
		// if no agent properties given then use spawner to determine player class and position
		if(playerAgentProperties == null) {
			String initPlayClass = spawner.getProperty(CommonKV.Spawn.KEY_PLAYER_AGENTCLASS, null, String.class);
			if(initPlayClass == null)
				return null;
			ObjectProperties playerAP = Agent.createPointAP(initPlayClass, spawner.getPosition());
			if(spawner.getProperty(CommonKV.KEY_DIRECTION, "", String.class).equals(CommonKV.VAL_RIGHT))
				playerAP.put(CommonKV.KEY_DIRECTION, Direction4.RIGHT);
			return (PlayerAgent) agency.createAgent(playerAP);
		}
		// otherwise use agent properties and set start point to main spawn point
		else {
			playerAgentProperties.put(AgencyKV.Spawn.KEY_START_POINT, spawner.getPosition());
			return (PlayerAgent) agency.createAgent(playerAgentProperties);
		}
	}

	private Agent getMainPlayerSpawner() {
		// find main spawnpoint and spawn player there, or spawn at (0, 0) if no spawnpoint found
		Collection<Agent> spawnList = agency.getAgentsByProperties(
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS, CommonKV.Spawn.KEY_SPAWN_MAIN },
				new String[] { CommonKV.AgentClassAlias.VAL_PLAYERSPAWNER, CommonKV.VAL_TRUE });
		if(!spawnList.isEmpty())
			return spawnList.iterator().next();
		else
			return null;
	}

	// safely get the view center - cannot return null, and tries to return a correct view center
	private Vector2 getViewCenter() {
		Vector2 vc = null;
		if(playerAgent != null)
			vc = playerAgent.getSupervisor().getViewCenter();
		if(vc == null)
			vc = lastViewCenter;
		else
			lastViewCenter.set(vc);
		return vc;
	}

	@Override
	public void dispose() {
		if(scrollBox != null)
			scrollBox.dispose();
		if(keepAliveBox != null)
			keepAliveBox.dispose();
		if(spawnTrigger != null)
			spawnTrigger.dispose();
		if(playerAgent != null)
			playerAgent.dispose();
		doStopMainMusic();
		stageHUD.clear();
	}
}
