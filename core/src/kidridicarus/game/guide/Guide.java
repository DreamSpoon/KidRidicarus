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
import kidridicarus.common.agent.PlayerAgent;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.keepalivebox.KeepAliveBox;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.MoveAdvice;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.powerup.SMB_Pow;
import kidridicarus.game.tool.KeyboardMapping;
import kidridicarus.game.tool.QQ;

/*
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

	private String currentMainMusicName;
	private Music currentMainMusic;
	private boolean isMainMusicPlaying;
	private Music currentSinglePlayMusic;
	private Vector2 lastViewCenter;

	// TODO this is hack - remove!
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
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP))
			Powerup.tryPushPowerup(playerAgent, new SMB_Pow.FireFlowerPow());
	}

	public void preUpdateAgency(float delta) {
		// get user input
		handleInput();

		// ensure spawn trigger and keep alive box follow view center
		spawnTrigger.setTarget(getViewCenter());
		keepAliveBox.setTarget(getViewCenter());
		// pass user input to player agent's supervisor
		playerAgent.getSupervisor().setMoveAdvice(inputMoveAdvice);
		playerAgent.getSupervisor().preUpdateAgency(delta);
	}

	// check / do player agent power character changes 
	public void updateAgency() {
		// check for "out-of-character" powerup received and change to appropriate character for powerup
		Powerup nonCharPowerup = playerAgent.getSupervisor().getNonCharPowerups().getFirst();
		playerAgent.getSupervisor().clearNonCharPowerups();
		if(nonCharPowerup != null)
			switchAgentType(nonCharPowerup.getPowerupCharacter());
	}

	public void postUpdateAgency() {
		playerAgent.getSupervisor().postUpdateAgency();
	}

	private void switchAgentType(PowChar pc) {
		Vector2 currentPos = new Vector2(0f, 0f);
		boolean facingRight = false;
		if(playerAgent != null) {
			currentPos = playerAgent.getPosition().add(SAFETY_RESPAWN_OFFSET);
			facingRight = playerAgent.getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE,
					Direction4.class) == Direction4.RIGHT;
			agency.disposeAgent(playerAgent);
			playerAgent = null;
		}

		switch(pc) {
			default:
			case MARIO:
				doAgentMake(GameKV.SMB.AgentClassAlias.VAL_MARIO, currentPos, facingRight);
				break;
			case SAMUS:
				doAgentMake(GameKV.Metroid.AgentClassAlias.VAL_SAMUS, currentPos, facingRight);
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

	public void postRenderFrame() {
		playerAgent.getSupervisor().drawHUD();
	}

	public boolean isGameWon() {
		return playerAgent.getSupervisor().isAtLevelEnd();
	}

	public boolean isGameOver() {
		return playerAgent.getSupervisor().isGameOver();
	}

	public Ear createEar() {
		return new Ear() {
			@Override
			public void registerMusic(String musicName) { doRegisterMusic(musicName); }
			@Override
			public void playSound(String soundName) { doPlaySound(soundName); }
			@Override
			public void changeAndStartMainMusic(String musicName) { doChangeAndStartMainMusic(musicName); };
			@Override
			public void startSinglePlayMusic(String musicName) { doStartSinglePlayMusic(musicName); }
			@Override
			public void stopAllMusic() { doStopAllMusic(); };
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
		// find main player spawner and return fail if none found
		Agent spawner = getMainPlayerSpawner();
		if(spawner == null)
			return false;

		// spawn player with properties at spawn location
		playerAgent = spawnPlayerAgentWithProperties(playerAgentProperties, spawner);
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
			String initPlayClass = spawner.getProperty(CommonKV.Spawn.KEY_PLAYERAGENTCLASS, null, String.class);
			if(initPlayClass == null)
				return null;
			return (PlayerAgent) agency.createAgent(Agent.createPointAP(initPlayClass, spawner.getPosition()));
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
				new String[] { AgencyKV.Spawn.KEY_AGENTCLASS, CommonKV.Spawn.KEY_SPAWNMAIN },
				new String[] { CommonKV.AgentClassAlias.VAL_PLAYERSPAWNER, CommonKV.VAL_TRUE });
		if(!spawnList.isEmpty())
			return spawnList.iterator().next();
		else
			return null;
	}

	// safely get the view center - cannot return null, and tries to return a correct view center
	private Vector2 getViewCenter() {
		Vector2 vc = playerAgent.getSupervisor().getViewCenter();
		if(vc == null)
			vc = lastViewCenter;
		else
			lastViewCenter.set(vc);
		return vc;
	}

	@Override
	public void dispose() {
		doStopMainMusic();
		stageHUD.clear();
	}
}
