package com.ridicarus.kid.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.InfoSMB;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.hud.SMB_Hud;
import com.ridicarus.kid.tools.QQ;
import com.ridicarus.kid.worldrunner.Player;
import com.ridicarus.kid.worldrunner.WorldRenderer;
import com.ridicarus.kid.worldrunner.WorldRunner;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private TextureAtlas atlas;
	private TmxMapLoader maploader;
	private TiledMap map;
	private SMB_Hud smbHud;
	private WorldRunner worldRunner;
	private WorldRenderer worldRenderer;
	private Player rePlayer;

	public PlayScreen(MyKidRidicarus game) {
		this.game = game;

		atlas = new TextureAtlas(GameInfo.TEXATLAS_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(GameInfo.P2M(GameInfo.V_WIDTH), GameInfo.P2M(GameInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2, gameport.getWorldHeight()/2, 0);

		maploader = new TmxMapLoader();
		map = maploader.load(GameInfo.GAMEMAP_FILENAME);

		worldRunner = new WorldRunner(game.manager, atlas, gamecam);
		worldRunner.loadMap(map, game.manager);
		// start renderer after loading map into runner, TODO: fix this
		worldRenderer = new WorldRenderer(worldRunner);

		rePlayer = worldRunner.createPlayer();

		smbHud = new SMB_Hud(game.batch, worldRunner, rePlayer);
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		worldRenderer.drawAll(game.batch, gamecam);

		// draw the HUD last, so it's on top of everything else
		smbHud.draw();

		QQ.renderTo(game.sr, gamecam.combined);

		// change to game over screen?
		if(gameWon()) {
			game.setScreen(new GameOverScreen(game, true));
			dispose();
		}
		else if(gameOver()) {
			game.setScreen(new GameOverScreen(game, false));
			dispose();
		}
	}

	// Update the game world.
	private void update(float delta) {
		handleInput(delta);

		// robots are updated in the worldrunner update method
		worldRunner.update(delta);

		smbHud.update(delta);
	}

	private void handleInput(float delta) {
		rePlayer.handleInput();
	}

	private boolean gameOver() {
		if(rePlayer.getRole().isDead() && rePlayer.getRole().getStateTimer() > InfoSMB.MARIO_DEAD_TIME)
			return true;
		return false;
	}

	private boolean gameWon() {
		if(rePlayer.getRole().isAtLevelEnd() && rePlayer.getRole().getStateTimer() > InfoSMB.MARIO_LEVELEND_TIME)
			return true;
		return false;
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
	}

	@Override
	public void dispose() {
		map.dispose();
		worldRunner.dispose();
		smbHud.dispose();
	}

	@Override
	public void show() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}
}
