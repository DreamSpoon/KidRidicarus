package kidridicarus.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.agency.AgentClassList;
import kidridicarus.common.agencydirector.AgencyDirector;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.GfxInfo;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.MyKidRidicarus;
import kidridicarus.game.info.AudioInfo;
import kidridicarus.game.info.GameInfo;
import kidridicarus.game.info.MetroidInfo;
import kidridicarus.game.info.SMBInfo;
import kidridicarus.game.play.PlayCoordinator;
import kidridicarus.game.tool.KeyboardMapping;
import kidridicarus.game.tool.QQ;

public class PlayScreen implements Screen {
	/*
	 * DEBUG: Forced update framerate
	 *   Example to explain forced update framerate:
	 * If the update function is called 30 times per second,
	 * and it is fed an update delta of 1/60 second then:
	 * 30 * 1/60 = 0.5 seconds
	 * So for each real-time 1 second, only 0.5 game world seconds have elapsed.
	 * The game world appears slow to a real-time observer.
	 * 
	 * Notes:
	 *   -forcedUpdateFPS, which defaults to FF_FPS, is variable
	 *   -FF_DELTA is not variable
	 */
	private static final boolean FF_USE = false;
	// how often the update function is called
	private static final float FF_FPS = 30f;
	// the delta time passed to the update function
	private static final float FF_DELTA = 1f/60f;

	private static final float MIN_FPS = 52f;
	private static final float MAX_FPS = 70f;

	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport gameport;
	private Stage stageHUD;
	private TextureAtlas atlas;
	private AgencyDirector director;
	private PlayCoordinator playCo;
	private int level;

	private boolean useForcedUpdateFramerate;
	private float forcedUpdateFPS;
	private float forcedUpdateFrameTimer;
	private Box2DDebugRenderer b2dr;

	public PlayScreen(MyKidRidicarus game, int level) {
		this.game = game;
		this.level = level;

		useForcedUpdateFramerate = FF_USE;
		forcedUpdateFPS = FF_FPS;
		forcedUpdateFrameTimer = 0f;

		atlas = new TextureAtlas(GameInfo.TA_MAIN_FILENAME);

		gamecam = new OrthographicCamera();
		gameport = new FitViewport(UInfo.P2M(GfxInfo.V_WIDTH), UInfo.P2M(GfxInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(gameport.getWorldWidth()/2f, gameport.getWorldHeight()/2f, 0);

		director = new AgencyDirector(game.manager, atlas, GfxInfo.KIDRID_DRAWORDER_ALIAS,
				new AgentClassList(CommonInfo.CORE_AGENT_CLASS_LIST, SMBInfo.SMB_AGENT_CLASSLIST,
						MetroidInfo.METROID_AGENT_CLASSLIST), AudioInfo.SOUND_VOLUME);
		director.createSpace(game.getLevelFilename(level));

		stageHUD = new Stage(new FitViewport(GfxInfo.V_WIDTH, GfxInfo.V_HEIGHT, new OrthographicCamera()),
				game.batch);

		b2dr = new Box2DDebugRenderer();

		playCo = new PlayCoordinator(director.getAgency(), game.manager, stageHUD);
		playCo.setPlayAgent(director.createInitialPlayerAgent());
	}

	@Override
	public void render(float delta) {
		update(delta);
		drawScreen();
	}

	private void update(float delta) {
		float newDelta;
		// if use forced update frame rate is enabled...
		if(processForcedUpdateFramerate()) {
			// ... and if an update frame is allowed then run the update
			if(pollForcedUpdateFrame(delta))
				newDelta = FF_DELTA;
			// ... else no update until allowed
			else
				return;
		}
		else
			newDelta = clampFrameDelta(delta);

		// pre-update stuff like scripts, and user input
		playCo.preUpdateAgency(newDelta);
		// update the game world
		director.update(newDelta);
		// post-update stuff like camera changes
		playCo.postUpdateAgency();
	}

	private float clampFrameDelta(float delta) {
		// If not using forced update frame rate then let the frame rate float between min and max fps
		// (the fps may get low when user moves the screen, something loads in the background, etc.)
		// This range of fps is needed by the game world to maintain regularity - e.g. the zoomer might
		//  'lurch' from one place to another.
		// TODO: Switch to constant 60 udpate fps and skip updates if render fps goes higher than 60 fps? 
		if(delta > 1f/MIN_FPS)
			return 1f/MIN_FPS;
		else if(delta < 1f/MAX_FPS)
			return 1f/MAX_FPS;
		return delta;
	}

	private boolean processForcedUpdateFramerate() {
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.FORCE_FRAMERATE_TOGGLE)) {
			useForcedUpdateFramerate = !useForcedUpdateFramerate;
			QQ.pr("useForcedFramerate=" + useForcedUpdateFramerate);
		}
		boolean updated=false;
		if(Gdx.input.isKeyJustPressed(KeyboardMapping.FORCE_FRAMERATE_FASTER)) {
			updated = true;
			forcedUpdateFPS *= 1.1f;
		}
		else if(Gdx.input.isKeyJustPressed(KeyboardMapping.FORCE_FRAMERATE_SLOWER)) {
			updated = true;
			forcedUpdateFPS /= 1.1f;
		}
		if(forcedUpdateFPS < 1f)
			forcedUpdateFPS = 1f;
		else if(forcedUpdateFPS > 60f)
			forcedUpdateFPS = 60f;
		if(updated)
			QQ.pr("Force framerate is set to " + String.format("%1.1f", forcedUpdateFPS) + " FPS");

		return useForcedUpdateFramerate;
	}

	private boolean pollForcedUpdateFrame(float delta) {
		forcedUpdateFrameTimer += delta;
		if(forcedUpdateFrameTimer >= 1f / forcedUpdateFPS) {
			forcedUpdateFrameTimer -= 1f / forcedUpdateFPS;
			return true;
		}
		return false;
	}

	private void drawScreen() {
		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		playCo.updateCamera(gamecam);

		// draw screen
		director.draw(game.batch, gamecam);

		// DEBUG: draw outlines of Box2D fixtures
		if(QQ.isOn())
			b2dr.render(director.getAgency().getWorld(), gamecam.combined);

		// draw HUD last
		playCo.postRenderFrame();

		// change to next level?
		if(playCo.isGameWon()) {
			game.setScreen(new LevelTransitScreen(game, level+1));
			dispose();
		}
		// change to game over screen?
		else if(playCo.isGameOver()) {
			game.setScreen(new GameOverScreen(game, false));
			dispose();
		}
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
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

	@Override
	public void dispose() {
		playCo.dispose();
		stageHUD.dispose();
		director.dispose();
		b2dr.dispose();
	}
}
