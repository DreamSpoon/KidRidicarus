package kidridicarus.common.guide;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.AudioInfo;
import kidridicarus.common.metaagent.playeragentwrapper.PlayerControllerAgent;

/*
 * Guide AKA Player (not actually tho - eventually this class will split into Player and others).
 * Handles:
 *   -music changes
 *   -returning info about state of play.
 */
public class Guide implements Disposable {
	private AssetManager manager;
	private Agency agency;
	private PlayerControllerAgent playerWrapper;
	private Eye eye;
	private Ear ear;

	private String currentMainMusicName;
	private Music currentMainMusic;
	private boolean isMainMusicPlaying;
	private Music currentSinglePlayMusic;

	public Guide(AssetManager manager, Batch batch, OrthographicCamera camera, Agency agency) {
		this.manager = manager;
		this.agency = agency;

		playerWrapper = null;
		currentMainMusicName = "";
		currentMainMusic = null;
		isMainMusicPlaying = false;
		currentSinglePlayMusic = null;
		agency.setEar(createEar());
		agency.setEye(createEye(batch, camera));
	}

	// create an Ear to give to Agency, so that Guide can receive sound/music callbacks from Agency
	public Ear createEar() {
		ear = new Ear() {
			@Override
			public void registerMusic(String musicName) {
				manager.load(musicName, Music.class);
				manager.finishLoading();
			}
			@Override
			public void playSound(String soundName) {
				manager.get(soundName, Sound.class).play(AudioInfo.SOUND_VOLUME);
			}
			@Override
			public void changeAndStartMainMusic(String musicName) { doChangeAndStartMainMusic(musicName); }
			@Override
			public void startSinglePlayMusic(String musicName) { doStartSinglePlayMusic(musicName); }
			@Override
			public void stopAllMusic() { doStopAllMusic(); }
		};
		return ear;
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

	private Eye createEye(Batch batch, OrthographicCamera camera) {
		if(eye != null)
			throw new IllegalStateException("Cannot create second eye.");
		eye = new Eye(batch, camera);
		return eye;
	}

	public void createPlayerAgent(ObjectProperties playerAgentProperties) {
		playerWrapper = (PlayerControllerAgent) agency.createAgent(PlayerControllerAgent.makeAP(playerAgentProperties));
	}

	public boolean isGameWon() {
		return playerWrapper.isGameWon();
	}

	public boolean isGameOver() {
		return playerWrapper.isGameOver();
	}

	public String getNextLevelFilename() {
		return playerWrapper.getNextLevelFilename();
	}

	public ObjectProperties getCopyPlayerAgentProperties() {
		return playerWrapper.getCopyPlayerAgentProperties();
	}

	@Override
	public void dispose() {
		if(ear != null) {
			doStopMainMusic();
			agency.setEar(null);
			ear = null;
		}
		if(eye != null) {
			eye.dispose();
			agency.setEye(null);
		}
	}
}
