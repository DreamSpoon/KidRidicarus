package kidridicarus.game.agent.Metroid.NPC.skree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.common.info.UInfo;
import kidridicarus.game.agent.Metroid.NPC.skree.Skree.MoveState;
import kidridicarus.game.info.MetroidGfx;

public class SkreeSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(24);
	private static final float ANIM_SPEED_REG = 0.17f;
	private static final float ANIM_SPEED_FAST = ANIM_SPEED_REG / 3f;

	private static final Vector2 SPECIAL_OFFSET = UInfo.VectorP2M(0f, 2f);

	// regular and fast spinning animations
	private Animation<TextureRegion> spinAnim;
	private Animation<TextureRegion> spinFastAnim;
	private Animation<TextureRegion> injuryAnim;

	private float stateTimer;

	public SkreeSprite(TextureAtlas atlas, Vector2 position) {
		spinAnim = new Animation<TextureRegion>(ANIM_SPEED_REG,
				atlas.findRegions(MetroidGfx.NPC.SKREE), PlayMode.LOOP);
		spinFastAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidGfx.NPC.SKREE), PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidGfx.NPC.SKREE_HIT), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(spinAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}

	public void update(float delta, Vector2 position, MoveState parentState) {
		switch(parentState) {
			case SLEEP:
			default:
				setRegion(spinAnim.getKeyFrame(stateTimer));
				break;
			case FALL:
			case ONGROUND:
				setRegion(spinFastAnim.getKeyFrame(stateTimer));
				break;
			case INJURY:
				setRegion(injuryAnim.getKeyFrame(stateTimer));
				break;
			case DEAD:
				break;
		}

		stateTimer += delta;
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}
}
