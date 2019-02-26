package kidridicarus.game.agent.SMB.player;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.agent.AgentObserver;

public class MarioObserver extends AgentObserver {
	private MarioHUD marioHUD;
	private Mario mario;
	private TextureAtlas atlas;

	public MarioObserver(Mario mario, TextureAtlas atlas) {
		super(mario);
		this.mario = mario;
		this.atlas = atlas;
	}

	@Override
	public void setStageHUD(Stage stageHUD) {
		marioHUD = new MarioHUD(mario, atlas, stageHUD);
	}

	@Override
	public void drawHUD() {
		marioHUD.draw();
	}
}
