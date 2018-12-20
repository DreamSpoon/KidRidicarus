package kidridicarus.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.MyKidRidicarus;
import kidridicarus.agencydirector.SMBGuide;
import kidridicarus.agencydirector.AgencyDirector;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private TextureAtlas atlas;
	private AgencyDirector director;
	private SMBGuide guide;	// "player"

	public PlayScreen(MyKidRidicarus game) {
		this.game = game;

		atlas = new TextureAtlas(GameInfo.TEXATLAS_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(UInfo.P2M(GameInfo.V_WIDTH), UInfo.P2M(GameInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2f, gameport.getWorldHeight()/2f, 0);

		director = new AgencyDirector(game.manager, atlas);
		director.createSpace(GameInfo.GAMEMAP_FILENAME);
		guide = director.createGuide(game.batch, gamecam);
	}

	@Override
	public void render(float delta) {
		update(delta);

		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw screen
		director.draw(guide);

		// change to game over screen?
		if(guide.isGameWon()) {
			game.setScreen(new GameOverScreen(game, true));
			dispose();
		}
		else if(guide.isGameOver()) {
			game.setScreen(new GameOverScreen(game, false));
			dispose();
		}
	}

	// Update the game world.
	private void update(float delta) {
		guide.handleInput();
		director.update(delta);
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
	}

	@Override
	public void dispose() {
		director.dispose();
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
