package kidridicarus.game.Metroid.agent.player;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.common.agent.GameAgentObserver;

public class SamusObserver extends GameAgentObserver {
	private SamusHUD samusHUD;
	private TextureAtlas atlas;

	public SamusObserver(Samus samus, TextureAtlas atlas) {
		super(samus);
		this.atlas = atlas;
	}

	@Override
	public void setStageHUD(Stage stageHUD) {
		samusHUD = new SamusHUD((Samus) playerAgent, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		samusHUD.draw();
	}
}
