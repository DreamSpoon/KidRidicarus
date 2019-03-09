package kidridicarus.game.SMB.agent.player;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.common.agent.GameAgentObserver;
import kidridicarus.game.SMB.agent.player.HUD.MarioHUD;

public class MarioObserver extends GameAgentObserver {
	private MarioHUD marioHUD;
	private TextureAtlas atlas;

	public MarioObserver(Mario mario, TextureAtlas atlas) {
		super(mario);
		this.atlas = atlas;
	}

	@Override
	public void setStageHUD(Stage stageHUD) {
		marioHUD = new MarioHUD((Mario) playerAgent, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		marioHUD.draw();
	}
}
