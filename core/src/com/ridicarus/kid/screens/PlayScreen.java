package com.ridicarus.kid.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;
import com.ridicarus.kid.roles.Player;
import com.ridicarus.kid.scenes.Hud;
import com.ridicarus.kid.tools.WorldContactListener;
import com.ridicarus.kid.tools.WorldRunner;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;

	private OrthographicCamera gamecam;
	private Viewport gameport;

	private TextureAtlas atlas;
	private Hud hud;
	private TmxMapLoader maploader;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;

	private World world;
	private Box2DDebugRenderer b2dr;

	private WorldRunner worldRunner;

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
		renderer = new OrthogonalTiledMapRenderer(map, GameInfo.P2M(1f));

		world = new World(new Vector2(0, -10f), true);
		b2dr = new Box2DDebugRenderer();

		worldRunner = new WorldRunner(this);
		worldRunner.loadMap(map);

		rePlayer = new Player(this);
		worldRunner.setPlayer(rePlayer);

		world.setContactListener(new WorldContactListener());

		startMusic();
	}

	private void startMusic() {
		music = MyKidRidicarus.manager.get(GameInfo.MUSIC_MARIO, Music.class);
		music.setLooping(true);
		music.setVolume(GameInfo.MUSIC_VOLUME);
		music.play();
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
	}

	// Update the game world.
	public void update(float delta) {
		handleInput(delta);

		world.step(delta, 6, 2);

		// update player
		rePlayer.update(delta);

		// robots are updated in the worldrunner update method
		worldRunner.update(delta, rePlayer.getRole());

		hud.update(delta);

		// camera follows player horizontally, unless player is dead
		if(!rePlayer.getRole().isDead())
			gamecam.position.x = rePlayer.getRole().getB2Body().getPosition().x;

		gamecam.update();
		renderer.setView(gamecam);
	}

	private void handleInput(float delta) {
		rePlayer.handleInput();

		// DEBUG: If A is pressed then create a tile at the x position of the player, and y = 3
		if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			int BLANK_COIN = 28;
			TiledMapTileSet tileSet = map.getTileSets().getTileSet(GameInfo.TILESET_GUTTER);
			worldRunner.createTile((int) (GameInfo.M2P(gamecam.position.x) / GameInfo.TILEPIX_X),  3,  tileSet.getTile(BLANK_COIN));
		}
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw tiles of world
		renderer.render();

		// draw debug lines for physics of world
//		b2dr.render(world, gamecam.combined);

		game.batch.setProjectionMatrix(gamecam.combined);
		game.batch.begin();
		worldRunner.draw(game.batch);
		game.batch.end();

		// draw the HUD last, so it's on top of everything else
		game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
		hud.stage.draw();

		// change to game over screen?
		if(gameOver()) {
			game.setScreen(new GameOverScreen(game));
			dispose();
		}
	}

	public boolean gameOver() {
		if(rePlayer.getRole().isDead() && rePlayer.getRole().getStateTimer() > GameInfo.MARIO_DEAD_TIME)
			return true;
		return false;
	}

	public void resize(int width, int height) {
		gameport.update(width, height);
	}

	public TiledMap getMap() {
		return map;
	}

	public World getWorld() {
		return world;
	}

	public WorldRunner getWorldRunner() {
		return worldRunner;
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

	@Override
	public void dispose() {
		map.dispose();
		renderer.dispose();
		world.dispose();
		b2dr.dispose();
		hud.dispose();
	}
}
