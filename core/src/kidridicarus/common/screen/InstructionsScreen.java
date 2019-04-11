package kidridicarus.common.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.agency.tool.Eye;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.KeyboardMapping;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metaagent.tiledmap.TiledMapMetaAgent;
import kidridicarus.game.MyKidRidicarus;

public class InstructionsScreen implements Screen {
	private MyKidRidicarus game;
	private OrthographicCamera gamecam;
	private Viewport viewport;
	private Stage stage;
	private InputProcessor oldInPr;

	private String nextLevelFilename;
	private boolean goRedTeamGo;
	private Eye myEye;

	public InstructionsScreen(MyKidRidicarus game, String nextLevelFilename) {
		this.game = game;
		this.nextLevelFilename = nextLevelFilename;
		goRedTeamGo = false;

		gamecam = new OrthographicCamera();
		viewport = new FitViewport(UInfo.P2M(CommonInfo.V_WIDTH), UInfo.P2M(CommonInfo.V_HEIGHT), gamecam);
		// set position so bottom left of view screen is (0, 0) in Box2D world 
		gamecam.position.set(viewport.getWorldWidth()/2f, viewport.getWorldHeight()/2f, 0);
		// camera for stage is different from camera for game world
		stage = new Stage(new FitViewport(CommonInfo.V_WIDTH, CommonInfo.V_HEIGHT, new OrthographicCamera()),
				game.batch);
		setupStage();

		oldInPr = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(new MyLittleInPr());

		// load the game map
		game.agency.createAgent(TiledMapMetaAgent.makeAP((new TmxMapLoader()).load(CommonInfo.INSTRO_FILENAME)));
		// run one update to let the map create the solid tile map and draw layer agents
		game.agency.update(1f/60f);
		// run a second update for the map to create the other agents (e.g. player spawner, rooms)
		game.agency.update(1f/60f);

		myEye = new Eye(game.batch, gamecam);
		game.agency.setEye(myEye);
	}

	private void setupStage() {
		BitmapFont realFont = new BitmapFont();
		LabelStyle labelFont = new LabelStyle(realFont, Color.WHITE);

		Table table = new Table();
		table.center();
		table.setFillParent(true);
		Label instructionsLabel = new Label(getInstructionsString(), labelFont);
		table.add(instructionsLabel).expandX().padTop(10f);
		table.row();
		Label playAgainLabel = new Label("press SPACE to play", labelFont);
		table.add(playAgainLabel).expandX().padTop(10f);

		stage.addActor(table);
	}

	private CharSequence getInstructionsString() {
		return "KEY - ACTION\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_LEFT).toUpperCase() + "  - move LEFT\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_RIGHT).toUpperCase() + "  - move RIGHT\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_UP).toUpperCase() + "  - move UP\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_DOWN).toUpperCase() + "  - move DOWN\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_RUNSHOOT).toUpperCase() + "  - run/shoot\n" +
				Input.Keys.toString(KeyboardMapping.MOVE_JUMP).toUpperCase() + "  - jump\n";
	}

	private class MyLittleInPr implements InputProcessor {
		// return true for all the following to relay that the event was handled
		@Override
		public boolean keyDown(int keycode) { return true; }
		@Override
		public boolean keyUp(int keycode) { return doKeyUp(keycode); }
		@Override
		public boolean keyTyped(char character) { return true; }
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) { return true; }
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) { return true; }
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) { return true; }
		@Override
		public boolean mouseMoved(int screenX, int screenY) { return true; }
		@Override
		public boolean scrolled(int amount) { return true; }
	}

	@Override
	public void show() {
	}

	private boolean doKeyUp(int keycode) {
		if(keycode == Input.Keys.SPACE)
			goRedTeamGo = true;
			
		return true;
	}

	@Override
	public void render(float delta) {
		update(delta);
		drawScreen();
	}

	private void update(float delta) {
		game.agency.update(delta);
		myEye.setViewCenter(UInfo.VectorP2M(CommonInfo.V_WIDTH/2f, CommonInfo.V_HEIGHT/2f));
	}

	private void drawScreen() {
		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw screen
		game.agency.draw();

		// draw HUD last
		stage.draw();

		if(goRedTeamGo) {
			dispose();
			game.setScreen(new PlayScreen((MyKidRidicarus) game, nextLevelFilename, null));
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
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
		Gdx.input.setInputProcessor(oldInPr);
		stage.dispose();
		game.agency.disposeAndRemoveAllAgents();
	}
}
