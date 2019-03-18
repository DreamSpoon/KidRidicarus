package kidridicarus.game.agent.SMB.player.luigi;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.game.agent.SMB.player.luigi.HUD.LuigiHUD;

public class LuigiObserver extends GameAgentObserver {
	private LuigiHUD luigiHUD;
	private TextureAtlas atlas;

	public LuigiObserver(Luigi luigi, TextureAtlas atlas) {
		super(luigi);
		this.atlas = atlas;
	}

	@Override
	public void setStageHUD(Stage stageHUD) {
		luigiHUD = new LuigiHUD((Luigi) playerAgent, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		luigiHUD.draw();
	}
}
