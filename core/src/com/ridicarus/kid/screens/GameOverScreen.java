package com.ridicarus.kid.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.MyKidRidicarus;

public class GameOverScreen implements Screen {
	private Viewport viewport;
	private Stage stage;
	private Game game;

	public GameOverScreen(Game game, boolean win) {
		LabelStyle font;
		Label gameOverLabel, playAgainLabel;
		Table table;

		this.game = game;
		viewport = new FitViewport(GameInfo.V_WIDTH, GameInfo.V_HEIGHT, new OrthographicCamera());
		stage = new Stage(viewport, ((MyKidRidicarus) game).batch);

		font = new LabelStyle(new BitmapFont(), Color.WHITE);
		table = new Table();
		table.center();
		table.setFillParent(true);

		if(win)
			gameOverLabel = new Label("GAME WON!", font);
		else
			gameOverLabel = new Label("GAME OVER", font);
		playAgainLabel = new Label("Click to Play Again", font);

		table.add(gameOverLabel).expandX();
		table.row();
		table.add(playAgainLabel).expandX().padTop(10f);

		stage.addActor(table);
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		if(Gdx.input.justTouched()) {
			game.setScreen(new PlayScreen((MyKidRidicarus) game));
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
		stage.dispose();
	}
}
