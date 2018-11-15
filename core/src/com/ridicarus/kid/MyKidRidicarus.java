/*
 * Originally created by brentaureli on 9/14/15.
 * GitHub:
 *     https://github.com/BrentAureli/SuperMario
 * 
 * Retrieved from GitHub on Oct 17, 2018
 * Modified afterward by David Loucks.
*/

package com.ridicarus.kid;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ridicarus.kid.screens.PlayScreen;

public class MyKidRidicarus extends Game {
	public SpriteBatch batch;

	// DEBUG: static context AssetManager is not advised, refactor this. so that AssetManager is passed to functions that
	// need it.
	public static AssetManager manager;

	@Override
	public void create () {
		batch = new SpriteBatch();
		manager = new AssetManager();
		manager.load(GameInfo.MUSIC_MARIO, Music.class);
		manager.load(GameInfo.SOUND_COIN, Sound.class);
		manager.load(GameInfo.SOUND_BUMP, Sound.class);
		manager.load(GameInfo.SOUND_BREAK, Sound.class);
		manager.load(GameInfo.SOUND_POWERUP_SPAWN, Sound.class);
		manager.load(GameInfo.SOUND_POWERUP_USE, Sound.class);
		manager.load(GameInfo.SOUND_POWERDOWN, Sound.class);
		manager.load(GameInfo.SOUND_STOMP, Sound.class);
		manager.load(GameInfo.SOUND_MARIODIE, Sound.class);
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
		manager.dispose();
	}
}
