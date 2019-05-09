package kidridicarus.game.SMB1.agent.player.mario.HUD;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.common.agent.playeragent.playerHUD.AnimationActor;
import kidridicarus.common.agent.playeragent.playerHUD.PlayerHUD;
import kidridicarus.game.info.SMB1_Gfx;
import kidridicarus.game.info.SMB1_KV;

public class MarioHUD extends PlayerHUD {
	private static final float COIN_ANIM_SPEED = 0.133f;

	private Agent playerAgent;
	private TextureAtlas atlas;

	private Label scoreVarLabel;
	private Label coinVarLabel;
	private AnimationActor animatingCoin;

	private FrameTime frameTime;

	public MarioHUD(Agent playerAgent, TextureAtlas atlas) {
		this.playerAgent = playerAgent;
		this.atlas = atlas;
		frameTime = new FrameTime();
	}

	@Override
	protected void setupStage(Stage stage) {
		Table table = new Table();
		table.top();
		table.setFillParent(true);

		LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(Gdx.files.internal(SMB1_Gfx.SMB1_FONT), false),
				Color.WHITE);
		Label marioLabel = new Label("MARIO", labelstyle);
		Label worldLabel = new Label("WORLD", labelstyle);
		Label timeLabel = new Label("TIME", labelstyle);
		scoreVarLabel = new Label(String.format("%06d", 0), labelstyle);
		coinVarLabel = new Label(String.format("×%02d", 0), labelstyle);

		table.add(marioLabel).align(Align.left).colspan(3).expandX().padLeft(24).padTop(16);
		table.add(worldLabel).align(Align.left).expandX().padTop(16);
		table.add(timeLabel).align(Align.left).expandX().padTop(16);
		table.row();
		table.add(scoreVarLabel).align(Align.left).expandX().padLeft(24);

		animatingCoin = new AnimationActor(new Animation<TextureRegion>(COIN_ANIM_SPEED,
				atlas.findRegions(SMB1_Gfx.General.HUD_COIN), PlayMode.LOOP));
		table.add(animatingCoin).align(Align.right);

		table.add(coinVarLabel).align(Align.left).expandX();
		table.add(new Label("1-1", labelstyle)).align(Align.left).expandX();
		table.add(new Label(String.format("%03d", 0), labelstyle)).align(Align.left).expandX();

		stage.addActor(table);
	}

	public void update(FrameTime frameTime) {
		this.frameTime = frameTime;
	}

	@Override
	protected void preDrawStage() {
		scoreVarLabel.setText(String.format("%06d",
				playerAgent.getProperty(SMB1_KV.KEY_POINTAMOUNT, 0, Integer.class)));
		coinVarLabel.setText(String.format("×%02d",
				playerAgent.getProperty(SMB1_KV.KEY_COINAMOUNT, 0, Integer.class)));
		animatingCoin.setStateTimer(frameTime.timeAbs);
	}
}
