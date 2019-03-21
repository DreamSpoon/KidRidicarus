package kidridicarus.game.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.MyKidRidicarus;

public class LevelTransitScreen implements Screen {
	private Viewport viewport;
	private Stage stage;
	private Game game;
	private InputProcessor oldInPr;
	private boolean didAnythingHappen;
	private String nextLevelFilename;
	private ObjectProperties playerAgentProperties;

	public LevelTransitScreen(MyKidRidicarus game, String nextLevelFilename, ObjectProperties playerAgentProperties) {
		this.game = game;
		this.nextLevelFilename = nextLevelFilename;
		this.playerAgentProperties = playerAgentProperties;

		viewport = new FitViewport(CommonInfo.V_WIDTH, CommonInfo.V_HEIGHT, new OrthographicCamera());
		stage = new Stage(viewport, game.batch);

		LabelStyle font = new LabelStyle(new BitmapFont(), Color.WHITE);
		Label gameOverLabel = new Label("Next Level: " + nextLevelFilename, font);
		Label playAgainLabel = new Label("Do Something to Play Next Level", font);
		Table table = new Table();
		table.center();
		table.setFillParent(true);
		table.add(gameOverLabel).expandX();
		table.row();
		table.add(playAgainLabel).expandX().padTop(10f);

		stage.addActor(table);

		didAnythingHappen = false;
		oldInPr = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(new MyLittleInPr());
	}

	private class MyLittleInPr implements InputProcessor {
		private boolean a() { return didAnythingHappen = true; }
		// return true for all the following to relay that the event was handled
		@Override
		public boolean keyDown(int keycode) { return a(); }
		@Override
		public boolean keyUp(int keycode) { return a(); }
		@Override
		public boolean keyTyped(char character) { return a(); }
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) { return a(); }
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) { return a(); }
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) { return a(); }
		@Override
		public boolean mouseMoved(int screenX, int screenY) { return a(); }
		@Override
		public boolean scrolled(int amount) { return a(); }
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		if(didAnythingHappen) {
			game.setScreen(new PlayScreen((MyKidRidicarus) game, nextLevelFilename, playerAgentProperties));
			dispose();
		}

		Gdx.gl.glClearColor(0,  0,  0,  1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
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
	}
}
