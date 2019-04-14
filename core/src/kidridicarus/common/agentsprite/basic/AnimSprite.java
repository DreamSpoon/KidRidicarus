package kidridicarus.common.agentsprite.basic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class AnimSprite extends Sprite {
	private Animation<TextureRegion> anim;
	private float animTimer;

	public AnimSprite(Animation<TextureRegion> anim, float sprWidth, float sprHeight, Vector2 position) {
		this.anim = anim;
		animTimer = 0f;
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), sprWidth, sprHeight);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, boolean isDeltaAbsolute, Vector2 position) {
		if(isDeltaAbsolute)
			setRegion(anim.getKeyFrame(delta));
		else {
			setRegion(anim.getKeyFrame(animTimer));
			animTimer += delta;
		}
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
