package kidridicarus.game.guide;

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
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agencydirector.AgencyDirector;
import kidridicarus.common.agent.agentspawntrigger.AgentSpawnTrigger;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.powerup.PowChar;
import kidridicarus.common.powerup.Powerup;
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
	private static final float SPAWN_TRIGGER_WIDTH = UInfo.P2M(UInfo.TILEPIX_X * 30);
	private static final float SPAWN_TRIGGER_HEIGHT = UInfo.P2M(UInfo.TILEPIX_X * 15);

	private Agency agency;

	// * two references to the same player agent - usage depends upon the functionality needed
	private Agent agent;
	private PlayerAgent playAgent;
	// * TODO ?  a private class that  extends Agent implements PlayerAgent  ?

	private AgentSpawnTrigger spawnTrigger;
	private MoveAdvice inputMoveAdvice;
	private Stage stageHUD;

	private String currentMainMusicName;
	private Music currentMainMusic;
	private boolean isMainMusicPlaying;
	private Music currentSinglePlayMusic;
	private AssetManager manager;
	// TODO this is hack - remove!
	private AgencyDirector director;

	public Guide(AgencyDirector director, Agency agency, AssetManager manager, Stage stageHUD) {
		this.director = director;
		this.agency = agency;
		this.manager = manager;
		this.stageHUD = stageHUD;

		spawnTrigger = null;
		playAgent = null;
		inputMoveAdvice = new MoveAdvice();
		currentMainMusicName = "";
		currentMainMusic = null;
		isMainMusicPlaying = false;
		currentSinglePlayMusic = null;
	}

	public void setGuidedAgent(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);

		this.agent = agent;
		this.playAgent = (PlayerAgent) agent;
		spawnTrigger = (AgentSpawnTrigger) agency.createAgent(AgentSpawnTrigger.makeAP(
				playAgent.getSupervisor().getViewCenter(), SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
		spawnTrigger.setEnabled(true);

		switchHUDtoNewPlayerAgent();
	}

	private void switchHUDtoNewPlayerAgent() {
		stageHUD.clear();
		playAgent.getSupervisor().setStageHUD(stageHUD);
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
			Powerup.tryPushPowerup(agent, new SMB_Pow.FireFlowerPow());
	}

	public void preUpdateAgency(float delta) {
		// get user input
		handleInput();

		// ensure spawn trigger follows view of player
		spawnTrigger.setTarget(playAgent.getSupervisor().getViewCenter());
		// pass user input to player agent's supervisor
		playAgent.getSupervisor().setMoveAdvice(inputMoveAdvice);
		playAgent.getSupervisor().preUpdateAgency(delta);
	}

	// check / do player agent power character changes 
	public void updateAgency() {
		// check for "out-of-character" powerup received and change to appropriate character for powerup
		Powerup nonCharPowerup = playAgent.getSupervisor().getNonCharPowerups().getFirst();
		playAgent.getSupervisor().clearNonCharPowerups();
		if(nonCharPowerup != null)
			switchAgentType(nonCharPowerup.getPowerupCharacter());
	}

	public void postUpdateAgency() {
		playAgent.getSupervisor().postUpdateAgency();
	}

	private void switchAgentType(PowChar pc) {
		Vector2 currentPos = new Vector2(0f, 0f);
		if(agent != null) {
			currentPos = agent.getPosition();
			agency.disposeAgent(agent);
			agent = null;
			playAgent = null;
		}

		switch(pc) {
			default:
			case MARIO:
				agent = agency.createAgent(Agent.createPointAP(GameKV.SMB.AgentClassAlias.VAL_MARIO, currentPos));
				playAgent = (PlayerAgent) agent;
				switchHUDtoNewPlayerAgent();
				break;
			case SAMUS:
				agent = agency.createAgent(Agent.createPointAP(GameKV.Metroid.AgentClassAlias.VAL_SAMUS, currentPos));
				playAgent = (PlayerAgent) agent;
				switchHUDtoNewPlayerAgent();
				break;
			case NONE:
				break;
		}
	}

	public void updateCamera(OrthographicCamera gamecam) {
		// if player is not dead then use their current room to determine the gamecam position
		if(!playAgent.getSupervisor().isGameOver()) {
			gamecam.position.set(playAgent.getSupervisor().getViewCenter(), 0f);
			gamecam.update();
		}
	}

	public void postRenderFrame() {
		playAgent.getSupervisor().drawHUD();
	}

	public boolean isGameWon() {
		return playAgent.getSupervisor().isAtLevelEnd();
	}

	public boolean isGameOver() {
		return playAgent.getSupervisor().isGameOver();
	}

	public Ear createEar() {
		return new Ear() {
			@Override
			public void onRegisterMusic(String musicName) { registerMusic(musicName); }
			@Override
			public void onPlaySound(String soundName) { playSound(soundName); }
			@Override
			public void onChangeAndStartMainMusic(String musicName) { doChangeAndStartMainMusic(musicName); };
			@Override
			public void onStartSinglePlayMusic(String musicName) { doStartSinglePlayMusic(musicName); }
			@Override
			public void stopAllMusic() { doStopAllMusic(); };
		};
	}

	private void playSound(String soundName) {
		manager.get(soundName, Sound.class).play(AudioInfo.SOUND_VOLUME);
	}

	// TODO This is a hack - registerMusic should be done before loading map file - read map file init agents
	// for rooms, then harvest music names.
	private void registerMusic(String musicName) {
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
		// stop single play music if playing
		if(currentSinglePlayMusic != null)
			currentSinglePlayMusic.stop();
	}

	public String getNextLevelFilename() {
		return playAgent.getSupervisor().getNextLevelFilename();
	}

	public ObjectProperties getCopyPlayerAgentProperties() {
		return playAgent.getCopyAllProperties();
	}

	@Override
	public void dispose() {
		doStopMainMusic();
		stageHUD.clear();
		// TODO the following code was giving an exception on game exit, where should player spawntrigger be disposed?
//		if(spawnTrigger != null)
//			spawnTrigger.dispose();
	}
}
