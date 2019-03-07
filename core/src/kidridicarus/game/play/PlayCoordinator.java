package kidridicarus.game.play;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.MoveAdvice;
import kidridicarus.common.agent.GameAgentSupervisor;
import kidridicarus.common.agent.AgentObserverPlus.AgentObserverListener;
import kidridicarus.common.agent.general.AgentSpawnTrigger;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameKV;
import kidridicarus.game.info.PowerupInfo.PowChar;
import kidridicarus.game.info.PowerupInfo.PowType;
import kidridicarus.game.info.SMBInfo;
import kidridicarus.game.tool.KeyboardMapping;
import kidridicarus.game.tool.QQ;

public class PlayCoordinator implements Disposable {
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

	public PlayCoordinator(Agency agency, AssetManager manager, Stage stageHUD) {
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

	public void setPlayAgent(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);

		this.agent = agent;
		this.playAgent = (PlayerAgent) agent;
		spawnTrigger = (AgentSpawnTrigger) agency.createAgent(AgentSpawnTrigger.makeAP(
				playAgent.getObserver().getViewCenter(), SPAWN_TRIGGER_WIDTH, SPAWN_TRIGGER_HEIGHT));
		spawnTrigger.setEnabled(true);

		setPlayAgentHUD();
	}

	private void setPlayAgentHUD() {
		playAgent.getObserver().setStageHUD(stageHUD);
		playAgent.getObserver().setListener(new AgentObserverListener() {
				@Override
				public void startSinglePlayMusic(String musicName) {
					doStartSinglePlayMusic(musicName);
				}

				@Override
				public void stopAllMusic() {
					doStopMainMusic();
				}

				@Override
				public void roomMusicUpdate(String musicName) {
					doChangeAndStartMainMusic(musicName);
				}
			});
	}

	public void handleInput() {
		inputMoveAdvice.moveRight = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RIGHT);
		inputMoveAdvice.moveUp = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_UP);
		inputMoveAdvice.moveLeft = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_LEFT);
		inputMoveAdvice.moveDown = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_DOWN);
		inputMoveAdvice.action0 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_RUNSHOOT);
		inputMoveAdvice.action1 = Gdx.input.isKeyPressed(KeyboardMapping.MOVE_JUMP);

		if(Gdx.input.isKeyJustPressed(KeyboardMapping.DEBUG_TOGGLE))
			QQ.toggleOn();
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.CHEAT_POWERUP)) {
			if(agent instanceof Mario) {
				((Mario) agent).applyPowerup(PowType.FIREFLOWER);
			}
		}
	}

	public void preUpdateAgency(float delta) {
		// get user input
		handleInput();

		// ensure spawn trigger follows view of player
		spawnTrigger.setTarget(playAgent.getObserver().getViewCenter());
		// pass user input to player agent's supervisor
		playAgent.getSupervisor().setMoveAdvice(inputMoveAdvice);
		playAgent.getSupervisor().preUpdateAgency(delta);
	}

	public void postUpdateAgency() {
		if(((GameAgentSupervisor) playAgent.getSupervisor()).isSwitchToOtherChar()) {
//			agency.startSinglePlayMusic(AudioInfo.Music.Metroid.METROIDITEM);
			switchAgentType(PowChar.SAMUS);
		}
		playAgent.getSupervisor().postUpdateAgency();
		playAgent.getObserver().postUpdateAgency();
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
				agent = agency.createAgent(Agent.createPointAP(GameKV.SMB.VAL_MARIO, currentPos));
				playAgent = (PlayerAgent) agent;
				setPlayAgentHUD();
				break;
			case SAMUS:
				agent = agency.createAgent(Agent.createPointAP(GameKV.Metroid.VAL_SAMUS, currentPos));
				playAgent = (PlayerAgent) agent;
				setPlayAgentHUD();
				break;
			case NONE:
				break;
		}
	}

	public void updateCamera(OrthographicCamera gamecam) {
		// if player is not dead then use their current room to determine the gamecam position
		if(!((PlayerAgent) agent).isDead()) {
			gamecam.position.set(playAgent.getObserver().getViewCenter(), 0f);
			gamecam.update();
		}
	}

	public void postRenderFrame() {
		playAgent.getObserver().drawHUD();
	}

	public boolean isGameWon() {
		if(playAgent.isAtLevelEnd())
			return true;
		return false;
	}

	public boolean isGameOver() {
		if(playAgent.isDead() && playAgent.getStateTimer() > SMBInfo.MARIO_DEAD_TIME)
			return true;
		return false;
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

	@Override
	public void dispose() {
		doStopMainMusic();
		stageHUD.clear();
//		if(spawnTrigger != null)
//			spawnTrigger.dispose();
	}
}
