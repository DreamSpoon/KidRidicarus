package com.ridicarus.kid.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.roles.Player;
import com.ridicarus.kid.scenes.Hud;
import com.ridicarus.kid.tools.WorldRenderer;
import com.ridicarus.kid.tools.WorldRunner;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private TextureAtlas atlas;
	private TmxMapLoader maploader;
	private TiledMap map;
	private Hud hud;
	private WorldRunner worldRunner;
	private WorldRenderer worldRenderer;
	private Player rePlayer;
	private Music music;

	public PlayScreen(MyKidRidicarus game) {
		this.game = game;

		atlas = new TextureAtlas(GameInfo.TEXATLAS_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(GameInfo.P2M(GameInfo.V_WIDTH), GameInfo.P2M(GameInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2, gameport.getWorldHeight()/2, 0);
		hud = new Hud(game.batch);

		maploader = new TmxMapLoader();
		map = maploader.load(GameInfo.GAMEMAP_NAME);

		worldRunner = new WorldRunner(atlas);
		worldRunner.loadMap(map);
		// start renderer after loading map into runner, TODO: fix this
		worldRenderer = new WorldRenderer(worldRunner);

		rePlayer = new Player(worldRunner);
		worldRunner.setPlayer(rePlayer);

		startMusic();
	}

	private void startMusic() {
		music = MyKidRidicarus.manager.get(GameInfo.MUSIC_MARIO, Music.class);
		music.setLooping(true);
		music.setVolume(GameInfo.MUSIC_VOLUME);
		music.play();
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		worldRenderer.drawAll(game.batch, gamecam);

		// draw the HUD last, so it's on top of everything else
		game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
		hud.stage.draw();

		// change to game over screen?
		if(gameOver()) {
			game.setScreen(new GameOverScreen(game));
			dispose();
		}
	}

	// Update the game world.
	private void update(float delta) {
		handleInput(delta);

		// robots are updated in the worldrunner update method
		worldRunner.update(delta);//, rePlayer.getRole());

		hud.update(delta);

		// camera follows player horizontally, unless player is dead
		if(!rePlayer.getRole().isDead())
			gamecam.position.x = rePlayer.getRole().getB2Body().getPosition().x;

		gamecam.update();
	}

	private void handleInput(float delta) {
		rePlayer.handleInput();

		// DEBUG: If A is pressed then create a tile at the x position of the player, and y = 3
//		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
//			int BLANK_COIN = 28;
//			TiledMapTileSet tileSet = map.getTileSets().getTileSet(GameInfo.TILESET_GUTTER);
//			worldRunner.createTile((int) (GameInfo.M2P(gamecam.position.x) / GameInfo.TILEPIX_X),  3,  tileSet.getTile(BLANK_COIN));
//		}
	}

	private boolean gameOver() {
		if(rePlayer.getRole().isDead() && rePlayer.getRole().getStateTimer() > GameInfo.MARIO_DEAD_TIME)
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
		hud.dispose();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
	}
}
