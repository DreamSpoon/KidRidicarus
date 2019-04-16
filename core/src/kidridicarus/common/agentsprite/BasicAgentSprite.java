package kidridicarus.common.agentsprite;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class BasicAgentSprite extends Sprite {
	private Animation<TextureRegion> anim;
	private float animTimer;

	public BasicAgentSprite(TextureRegion region, float sprWidth, float sprHeight, Vector2 position) {
		anim = null;
		setRegion(region);
		setBounds(getX(), getY(), sprWidth, sprHeight);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public BasicAgentSprite(Animation<TextureRegion> anim, float sprWidth, float sprHeight, Vector2 position) {
		this.anim = anim;
		animTimer = 0f;
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), sprWidth, sprHeight);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, boolean isDeltaAbsolute, boolean isFacingRight, Vector2 position) {
		if(anim != null) {
			if(isDeltaAbsolute)
				setRegion(anim.getKeyFrame(delta));
			else {
				setRegion(anim.getKeyFrame(animTimer));
				animTimer += delta;
			}
		}
		// should the sprite be flipped on X due to facing direction?
		if((isFacingRight && isFlipX()) || (!isFacingRight && !isFlipX()))
			flip(true,  false);
		setPosition(position.x - getWidth()/2, position.y - getHeight()/2);
	}
}
