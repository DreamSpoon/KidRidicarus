package kidridicarus.game.agent.KidIcarus.player.pit.HUD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.playerHUD.TexRegionActor;
import kidridicarus.game.info.KidIcarusGfx;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.info.SMB1_Gfx;

public class PitHUD implements Disposable {
	private PlayerAgent playerAgent;
	private Stage stage;

	private Label heartCountLabel;
	private HealthBarActor healthBar;

	public PitHUD(PlayerAgent playerAgent, TextureAtlas atlas, Stage stage) {
		this.playerAgent = playerAgent;
		this.stage = stage;

		Table table = new Table();
		table.top();
		table.setFillParent(true);

		LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(Gdx.files.internal(SMB1_Gfx.SMB1_FONT), false),
				Color.WHITE);
		heartCountLabel = new Label(String.format("%03d", 0), labelstyle);

		table.add(new TexRegionActor(atlas.findRegion(KidIcarusGfx.Item.HEART1))).
				align(Align.left).padLeft(16).padTop(16);
		table.add(heartCountLabel).align(Align.left).expandX().padTop(16);
		table.row();
		healthBar = new HealthBarActor(atlas);
		table.add(healthBar).align(Align.left).padLeft(16);

		stage.addActor(table);
	}

	private void update() {
		heartCountLabel.setText(String.format("%03d",
				playerAgent.getProperty(KidIcarusKV.KEY_HEARTS_COLLECTED, 0, Integer.class)));
		healthBar.setHealth(playerAgent.getProperty(KidIcarusKV.KEY_HEALTH, 0, Integer.class));
		stage.act();
	}

	public void draw() {
		update();
		stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
}
