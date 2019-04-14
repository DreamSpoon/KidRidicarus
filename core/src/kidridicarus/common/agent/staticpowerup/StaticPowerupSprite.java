package kidridicarus.common.agent.staticpowerup;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class StaticPowerupSprite extends Sprite {
	private Animation<TextureRegion> anim;
	private float stateTimer;

	public StaticPowerupSprite(Animation<TextureRegion> anim, float sprWidth, float sprHeight, Vector2 position) {
		this.anim = anim;
		stateTimer = 0f;
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), sprWidth, sprHeight);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, boolean isDeltaAbsolute, Vector2 position) {
		if(isDeltaAbsolute)
			setRegion(anim.getKeyFrame(delta));
		else
			setRegion(anim.getKeyFrame(stateTimer));
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
		stateTimer += delta;
	}
}
