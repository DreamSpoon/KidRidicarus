package kidridicarus.common.agent.playeragent.playerHUD;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public class AnimationDrawable extends BaseDrawable {
	private Animation<TextureRegion> animation;
	private float stateTimer;

	public AnimationDrawable(Animation<TextureRegion> animation) {
		setAnimation(animation);
		stateTimer = 0f;
	}

	public void setAnimation(Animation<TextureRegion> animation) {
		this.animation = animation;
		setMinWidth(animation.getKeyFrame(0f).getRegionWidth());
		setMinHeight(animation.getKeyFrame(0f).getRegionHeight());
	}

	public void setStateTimer(float stateTimer) {
		this.stateTimer = stateTimer;
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		batch.draw(animation.getKeyFrame(stateTimer), x, y, width, height);
	}
}
