package kidridicarus.game.KidIcarus.agent.player.pit.HUD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agent.playeragent.playerHUD.PlayerHUD;
import kidridicarus.common.agent.playeragent.playerHUD.TexRegionActor;
import kidridicarus.game.info.KidIcarusGfx;
import kidridicarus.game.info.KidIcarusKV;
import kidridicarus.game.info.SMB1_Gfx;

public class PitHUD extends PlayerHUD {
	private PlayerAgent playerAgent;
	private TextureAtlas atlas;

	private Label heartCountLabel;
	private HealthBarActor healthBar;

	public PitHUD(PlayerAgent playerAgent, TextureAtlas atlas) {
		this.playerAgent = playerAgent;
		this.atlas = atlas;
	}

	@Override
	public void setupStage(Stage stage) {
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

	@Override
	protected void preDrawStage() {
		heartCountLabel.setText(String.format("%03d",
				playerAgent.getProperty(KidIcarusKV.KEY_HEART_COUNT, 0, Integer.class)));
		healthBar.setHealth(playerAgent.getProperty(KidIcarusKV.KEY_HEALTH, 0, Integer.class));
	}
}
