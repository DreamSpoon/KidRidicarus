package kidridicarus.common.agent.playeragent.playerHUD;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.tool.Eye;

public abstract class PlayerHUD implements Disposable {
	private Stage hudStage = null;

	protected abstract void setupStage(Stage stage);
	protected abstract void preDrawStage();

	public void draw(Eye eye) {
		if(hudStage == null) {
			hudStage = eye.createStage();
			setupStage(hudStage);
		}
		hudStage.getBatch().setProjectionMatrix(hudStage.getCamera().combined);
		// stage is special, so end previous draw batch and let stage start a new one
		boolean isDrawing = eye.isDrawing();
		if(isDrawing)
			eye.end();
		preDrawStage();
		hudStage.act();
		hudStage.draw();
		if(isDrawing)
			eye.begin();
	}

	@Override
	public void dispose() {
		if(hudStage != null) {
			hudStage.dispose();
			hudStage = null;
		}
	}
}
