package kidridicarus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import kidridicarus.info.AudioInfo;
import kidridicarus.info.GameInfo;
import kidridicarus.screens.PlayScreen;

public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	public ShapeRenderer sr;

	public AssetManager manager;

	public String levelFilenames[] = new String[] {
			GameInfo.GAMEMAP_FILENAME1,
			GameInfo.GAMEMAP_FILENAME2 };

	@Override
	public void create () {
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
		manager = new AssetManager();
		// other music files may be loaded later when a space is loaded
		manager.load(AudioInfo.MUSIC_LEVELEND, Music.class);
		manager.load(AudioInfo.MUSIC_STARPOWER, Music.class);
		manager.load(AudioInfo.SOUND_COIN, Sound.class);
		manager.load(AudioInfo.SOUND_BUMP, Sound.class);
		manager.load(AudioInfo.SOUND_BREAK, Sound.class);
		manager.load(AudioInfo.SOUND_POWERUP_SPAWN, Sound.class);
		manager.load(AudioInfo.SOUND_POWERUP_USE, Sound.class);
		manager.load(AudioInfo.SOUND_POWERDOWN, Sound.class);
		manager.load(AudioInfo.SOUND_STOMP, Sound.class);
		manager.load(AudioInfo.SOUND_MARIODIE, Sound.class);
		manager.load(AudioInfo.SOUND_MARIOSMLJUMP, Sound.class);
		manager.load(AudioInfo.SOUND_MARIOBIGJUMP, Sound.class);
		manager.load(AudioInfo.SOUND_KICK, Sound.class);
		manager.load(AudioInfo.SOUND_FIREBALL, Sound.class);
		manager.load(AudioInfo.SOUND_FLAGPOLE, Sound.class);
		manager.load(AudioInfo.SOUND_1UP, Sound.class);
		manager.finishLoading();

		// start playing first level 
		setScreen(new PlayScreen(this, 0));
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
