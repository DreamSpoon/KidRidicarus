package kidridicarus.game.agent.sprite.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.Metroid.NPC.Skree.MoveState;
import kidridicarus.game.info.MetroidAnim;

public class SkreeSprite extends Sprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(24);
	private static final float ANIM_SPEED_REG = 0.17f;
	private static final float ANIM_SPEED_FAST = ANIM_SPEED_REG / 3f;

	private static final Vector2 SPECIAL_OFFSET = UInfo.P2MVector(0f, 2f);

	// regular and fast spinning animations
	private Animation<TextureRegion> spinAnim;
	private Animation<TextureRegion> spinFastAnim;
	private Animation<TextureRegion> injuryAnim;

	private float stateTimer;

	public SkreeSprite(TextureAtlas atlas, Vector2 position) {
		spinAnim = new Animation<TextureRegion>(ANIM_SPEED_REG,
				atlas.findRegions(MetroidAnim.NPC.SKREE), PlayMode.LOOP);
		spinFastAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidAnim.NPC.SKREE), PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidAnim.NPC.SKREE_HIT), PlayMode.LOOP);

		stateTimer = 0;

		setRegion(spinAnim.getKeyFrame(0f));
		// TODO: why +16f?
		setBounds(getX(), getY()+UInfo.P2M(16f), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}

	public void update(float delta, Vector2 position, MoveState curState) {
		switch(curState) {
			case SLEEP:
			default:
				setRegion(spinAnim.getKeyFrame(stateTimer, true));
				break;
			case FALL:
			case ONGROUND:
				setRegion(spinFastAnim.getKeyFrame(stateTimer, true));
				break;
			case INJURY:
				setRegion(injuryAnim.getKeyFrame(stateTimer, true));
				break;
			case DEAD:
				break;
		}

		stateTimer += delta;
		setPosition(position.x - getWidth()/2f + SPECIAL_OFFSET.x, position.y - getHeight()/2f + SPECIAL_OFFSET.y);
	}
}