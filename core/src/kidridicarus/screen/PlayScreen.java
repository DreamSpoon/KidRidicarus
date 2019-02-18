package kidridicarus.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.MyKidRidicarus;
import kidridicarus.agencydirector.AgencyDirector;
import kidridicarus.guide.MainGuide;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.info.PowerupInfo.PowChar;
import kidridicarus.tool.KeyboardMapping;
import kidridicarus.tool.QQ;

public class PlayScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private TextureAtlas atlas;
	private AgencyDirector director;
	private MainGuide guide;	// "player"
	private int level;

	// debug stuff
	private boolean useForcedFramerate = false;
	private float forcedFPS = 30f;
	private float frameTimer = 0;
	private float forcedDelta = 1f/60f;

	public PlayScreen(MyKidRidicarus game, int level, PowChar initPowChar) {
		this.game = game;
		this.level = level;

		atlas = new TextureAtlas(GameInfo.TA_MAIN_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(UInfo.P2M(GameInfo.V_WIDTH), UInfo.P2M(GameInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2f, gameport.getWorldHeight()/2f, 0);

		director = new AgencyDirector(game.manager, atlas);
		director.createSpace(game.getLevelFilename(level));
		guide = director.createGuide(game.batch, gamecam, initPowChar);
	}

	@Override
	public void render(float delta) {
		processForcedFramerate();
		if(useForcedFramerate) {
			if(pollForcedFrame(delta))
				update(forcedDelta);
		}
		else
			update(delta);

		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw screen
		director.draw(guide);

		// change to next level?
		if(guide.isGameWon()) {
			game.setScreen(new LevelTransitScreen(game, level+1));
			dispose();
		}
		// change to game over screen?
		else if(guide.isGameOver()) {
			game.setScreen(new GameOverScreen(game, false));
			dispose();
		}
	}

	private void processForcedFramerate() {
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.FORCE_FRAMERATE_TOGGLE)) {
			useForcedFramerate = !useForcedFramerate;
			QQ.pr("useForcedFramerate=" + useForcedFramerate);
		}
		boolean updated=false;
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.FORCE_FRAMERATE_FASTER)) {
			updated = true;
			forcedFPS *= 1.1f;
		}
		else if(Gdx.input.isKeyJustPressed(KeyboardMapping.FORCE_FRAMERATE_SLOWER)) {
			updated = true;
			forcedFPS /= 1.1f;
		}
		if(forcedFPS < 1f)
			forcedFPS = 1f;
		else if(forcedFPS > 60f)
			forcedFPS = 60f;
		if(updated)
			QQ.pr("Force framerate is set to " + String.format("%1.1f", forcedFPS) + " FPS");
	}

	private boolean pollForcedFrame(float delta) {
		frameTimer += delta;
		if(frameTimer >= 1f / forcedFPS) {
			frameTimer -= 1f / forcedFPS;
			return true;
		}
		return false;
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
