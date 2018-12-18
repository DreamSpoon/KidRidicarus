package kidridicarus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import kidridicarus.info.AudioInfo;
import kidridicarus.screens.PlayScreen;

public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	public ShapeRenderer sr;

	public AssetManager manager;

	@Override
	public void create () {
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
		manager = new AssetManager();
		// other musics may be loaded when a level loads
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
		setScreen(new PlayScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
		sr.dispose();
		manager.dispose();
	}
}
