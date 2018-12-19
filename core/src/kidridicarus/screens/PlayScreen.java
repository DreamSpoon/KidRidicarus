package kidridicarus.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.MyKidRidicarus;
import kidridicarus.agencydirector.Guide;
import kidridicarus.agencydirector.AgencyDirector;
import kidridicarus.hud.SMB_Hud;
import kidridicarus.info.GameInfo;
import kidridicarus.info.SMBInfo;
import kidridicarus.info.UInfo;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private TextureAtlas atlas;
	private SMB_Hud smbHud;
	private AgencyDirector director;
	private Guide rePlayer;

	public PlayScreen(MyKidRidicarus game) {
		this.game = game;

		atlas = new TextureAtlas(GameInfo.TEXATLAS_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(UInfo.P2M(GameInfo.V_WIDTH), UInfo.P2M(GameInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2, gameport.getWorldHeight()/2, 0);

		director = new AgencyDirector(game.manager, atlas, gamecam);
		director.createSpace(GameInfo.GAMEMAP_FILENAME);

		rePlayer = director.createGuide();

		smbHud = new SMB_Hud(game.batch, director.getAgency(), rePlayer);
	}

	@Override
	public void render(float delta) {
		update(delta);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		director.draw(game.batch);

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

		director.update(delta);

		smbHud.update(delta);
	}

	private void handleInput(float delta) {
		rePlayer.handleInput();
	}

	private boolean gameOver() {
		if(rePlayer.getAgent().isDead() && rePlayer.getAgent().getStateTimer() > SMBInfo.MARIO_DEAD_TIME)
			return true;
		return false;
	}

	private boolean gameWon() {
		if(rePlayer.getAgent().isAtLevelEnd() && rePlayer.getAgent().getStateTimer() > SMBInfo.MARIO_LEVELEND_TIME)
			return true;
		return false;
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
	}

	@Override
	public void dispose() {
		director.dispose();
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
