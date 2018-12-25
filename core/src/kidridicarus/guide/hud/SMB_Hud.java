package kidridicarus.guide.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import kidridicarus.guide.SMBGuide;
import kidridicarus.info.GameInfo;

public class SMB_Hud implements Disposable {
	private SMBGuide guide;
	private Stage stage;
	private Viewport viewport;

	private Label scoreVarLabel;
	private Label coinVarLabel;
	private Label worldVarLabel;
	private Label timeVarLabel;

	public SMB_Hud(Batch batch, TextureAtlas atlas, SMBGuide guide) {
		this.guide = guide;

		viewport = new FitViewport(GameInfo.V_WIDTH, GameInfo.V_HEIGHT, new OrthographicCamera());
		stage = new Stage(viewport, batch);

		Table table = new Table();
		table.top();
		table.setFillParent(true);

		LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(Gdx.files.internal(GameInfo.SMB1_FONT), false),
				Color.WHITE);
		Label marioLabel = new Label("MARIO", labelstyle);
		Label worldLabel = new Label("WORLD", labelstyle);
		Label timeLabel = new Label("TIME", labelstyle);
		scoreVarLabel = new Label(String.format("%06d", 0), labelstyle);
		coinVarLabel = new Label(String.format("×%02d", 0), labelstyle);
		worldVarLabel = new Label("1-1", labelstyle);
		timeVarLabel = new Label(String.format("%03d", 0), labelstyle);

		table.add(marioLabel).align(Align.left).colspan(3).expandX().padLeft(24).padTop(16);
		table.add(worldLabel).align(Align.left).expandX().padTop(16);
		table.add(timeLabel).align(Align.left).expandX().padTop(16);
		table.row();
		table.add(scoreVarLabel).align(Align.left).expandX().padLeft(24);
		table.add(new HudCoin(atlas)).align(Align.right);
		table.add(coinVarLabel).align(Align.left).expandX();
		table.add(worldVarLabel).align(Align.left).expandX();
		table.add(timeVarLabel).align(Align.left).expandX();

		stage.addActor(table);
	}

	public void setGuide(SMBGuide guide) {
		this.guide = guide;
	}

	public void update(float delta) {
		scoreVarLabel.setText(String.format("%06d", guide.getPointTotal()));
		timeVarLabel.setText(String.format("%03d", (int) guide.getLevelTimeRemaining()));
		coinVarLabel.setText(String.format("×%02d", guide.getCoinTotal()));
		stage.act();
	}

	public void draw() {
		stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
}
