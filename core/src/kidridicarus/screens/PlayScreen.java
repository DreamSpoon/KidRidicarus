package kidridicarus.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.MyKidRidicarus;
import kidridicarus.hud.SMB_Hud;
import kidridicarus.info.GameInfo;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.UInfo;
import kidridicarus.worldrunner.Player;
import kidridicarus.worldrunner.WorldRenderer;
import kidridicarus.worldrunner.WorldRunner;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private TextureAtlas atlas;
	private SMB_Hud smbHud;
	private WorldRunner worldRunner;
	private WorldRenderer worldRenderer;
	private Player rePlayer;

	public PlayScreen(MyKidRidicarus game) {
		this.game = game;

		atlas = new TextureAtlas(GameInfo.TEXATLAS_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(UInfo.P2M(GameInfo.V_WIDTH), UInfo.P2M(GameInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2, gameport.getWorldHeight()/2, 0);

		worldRunner = new WorldRunner(game.manager, atlas, gamecam);
		worldRunner.loadMap(GameInfo.GAMEMAP_FILENAME);
		// start renderer after loading map into runner, TODO: fix this
		worldRenderer = new WorldRenderer(worldRunner);

		rePlayer = worldRunner.createPlayer();

		smbHud = new SMB_Hud(game.batch, worldRunner.getSubWR(), rePlayer);
	}

	@Override
	public void render(float delta) {
		update(delta);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		worldRenderer.drawAll(game.batch, gamecam);
		// draw the HUD last, so it's on top of everything else
		smbHud.draw();

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
		if(rePlayer.getRole().isDead() && rePlayer.getRole().getStateTimer() > SMBInfo.MARIO_DEAD_TIME)
			return true;
		return false;
	}

	private boolean gameWon() {
		if(rePlayer.getRole().isAtLevelEnd() && rePlayer.getRole().getStateTimer() > SMBInfo.MARIO_LEVELEND_TIME)
			return true;
		return false;
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
	}

	@Override
	public void dispose() {
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
