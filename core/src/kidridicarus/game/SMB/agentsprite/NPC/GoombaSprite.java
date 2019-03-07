package kidridicarus.game.SMB.agentsprite.NPC;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.SMB.agent.NPC.Goomba.MoveState;
import kidridicarus.game.info.SMBAnim;

public class GoombaSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.4f;

	private Animation<TextureRegion> walkAnim;
	private TextureRegion squish;
	private float stateTimer;

	public GoombaSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(SMBAnim.NPC.GOOMBA_WALK), PlayMode.LOOP);
		squish = atlas.findRegion(SMBAnim.NPC.GOOMBA_SQUISH);

		stateTimer = 0;

		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	public void update(float delta, Vector2 position, MoveState moveState) {
		switch(moveState) {
			case SQUISH:
				setRegion(squish);
				break;
			case BUMP:
				// no walking after bopping
				setRegion(walkAnim.getKeyFrame(0f));
				// upside down when bopped
				if(!isFlipY())
					flip(false,  true);
				break;
			default:
				setRegion(walkAnim.getKeyFrame(stateTimer));
				break;
		}

		stateTimer += delta;

		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}
}
