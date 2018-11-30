package com.ridicarus.kid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.ridicarus.kid.screens.PlayScreen;

public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	public ShapeRenderer sr;

	public AssetManager manager;

	@Override
	public void create () {
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
		manager = new AssetManager();
//		manager.load(GameInfo.MUSIC_MARIO, Music.class);
		manager.load(GameInfo.MUSIC_LEVELEND, Music.class);
		manager.load(GameInfo.MUSIC_STARPOWER, Music.class);
		manager.load(GameInfo.SOUND_COIN, Sound.class);
		manager.load(GameInfo.SOUND_BUMP, Sound.class);
		manager.load(GameInfo.SOUND_BREAK, Sound.class);
		manager.load(GameInfo.SOUND_POWERUP_SPAWN, Sound.class);
		manager.load(GameInfo.SOUND_POWERUP_USE, Sound.class);
		manager.load(GameInfo.SOUND_POWERDOWN, Sound.class);
		manager.load(GameInfo.SOUND_STOMP, Sound.class);
		manager.load(GameInfo.SOUND_MARIODIE, Sound.class);
		manager.load(GameInfo.SOUND_MARIOSMLJUMP, Sound.class);
		manager.load(GameInfo.SOUND_MARIOBIGJUMP, Sound.class);
		manager.load(GameInfo.SOUND_KICK, Sound.class);
		manager.load(GameInfo.SOUND_FIREBALL, Sound.class);
		manager.load(GameInfo.SOUND_FLAGPOLE, Sound.class);
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
