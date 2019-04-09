package kidridicarus.common.agent.playeragent.playerHUD;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class AnimationActor extends Image {
	private AnimationDrawable animationDrawable;

	public AnimationActor(Animation<TextureRegion> animation) {
		super();
		animationDrawable = new AnimationDrawable(animation);
		setDrawable(animationDrawable);
	}

	public void setAnimation(Animation<TextureRegion> animation) {
		animationDrawable.setAnimation(animation);
	}

	public void setStateTimer(float stateTimer) {
		animationDrawable.setStateTimer(stateTimer);
	}
}
