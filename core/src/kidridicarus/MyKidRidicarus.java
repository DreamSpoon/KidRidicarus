package kidridicarus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo;
import kidridicarus.info.PowerupInfo.PowChar;
import kidridicarus.screen.PlayScreen;

/*
 * Main game asset loader class - or at least, it should be...
 * TODO: Load level assets here?
 */
public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	public ShapeRenderer sr;

	public AssetManager manager;

	private PowChar initPowChar = PowChar.MARIO;
	private String[] levelFilenames = new String[] {
			GameInfo.GAMEMAP_FILENAME1,
			GameInfo.GAMEMAP_FILENAME2 };

	@Override
	public void create () {
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
		manager = new AssetManager();
		// other music files may be loaded later when a space is loaded
		manager.load(AudioInfo.Music.SMB.LEVELEND, Music.class);
		manager.load(AudioInfo.Music.SMB.STARPOWER, Music.class);
		manager.load(AudioInfo.Music.Metroid.METROIDITEM, Music.class);
		manager.load(AudioInfo.Sound.SMB.COIN, Sound.class);
		manager.load(AudioInfo.Sound.SMB.BUMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.BREAK, Sound.class);
		manager.load(AudioInfo.Sound.SMB.POWERUP_SPAWN, Sound.class);
		manager.load(AudioInfo.Sound.SMB.POWERUP_USE, Sound.class);
		manager.load(AudioInfo.Sound.SMB.POWERDOWN, Sound.class);
		manager.load(AudioInfo.Sound.SMB.STOMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.MARIO_DIE, Sound.class);
		manager.load(AudioInfo.Sound.SMB.MARIO_SMLJUMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.MARIO_BIGJUMP, Sound.class);
		manager.load(AudioInfo.Sound.SMB.KICK, Sound.class);
		manager.load(AudioInfo.Sound.SMB.FIREBALL, Sound.class);
		manager.load(AudioInfo.Sound.SMB.FLAGPOLE, Sound.class);
		manager.load(AudioInfo.Sound.SMB.UP1, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.JUMP, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.STEP, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.HURT, Sound.class);
		manager.load(AudioInfo.Sound.Metroid.SHOOT, Sound.class);
		manager.finishLoading();

		// start playing first level 
		setScreen(new PlayScreen(this, 0, initPowChar));
	}

	@Override
	public void render () {
		super.render();
	}

	public String getLevelFilename(int level) {
		if(level < 0 || level >= levelFilenames.length)
			return "";
		return levelFilenames[level];
	}

	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
		sr.dispose();
		manager.dispose();
	}
}
