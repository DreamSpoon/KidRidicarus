package kidridicarus.game.agent.Metroid.player;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.agent.AgentObserver;

public class SamusObserver extends AgentObserver {
	private SamusHUD samusHUD;
	private Samus samus;
	private TextureAtlas atlas;

	public SamusObserver(Samus samus, TextureAtlas atlas) {
		super(samus);
		this.samus = samus;
		this.atlas = atlas;
	}

	@Override
	public void setStageHUD(Stage stageHUD) {
		samusHUD = new SamusHUD(samus, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		samusHUD.draw();
	}
}
