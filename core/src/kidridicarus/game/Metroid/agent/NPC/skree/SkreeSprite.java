package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.MetroidGfx;

public class SkreeSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(24);
	private static final Vector2 SPECIAL_OFFSET = UInfo.VectorP2M(0f, 2f);
	private static final float ANIM_SPEED_REG = 0.17f;
	private static final float ANIM_SPEED_FAST = ANIM_SPEED_REG / 3f;

	// regular and fast spinning animations
	private Animation<TextureRegion> spinAnim;
	private Animation<TextureRegion> spinFastAnim;
	private Animation<TextureRegion> injuryAnim;
	private float animTimer;

	public SkreeSprite(TextureAtlas atlas, Vector2 position) {
		spinAnim = new Animation<TextureRegion>(ANIM_SPEED_REG,
				atlas.findRegions(MetroidGfx.NPC.SKREE), PlayMode.LOOP);
		spinFastAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidGfx.NPC.SKREE), PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED_FAST,
				atlas.findRegions(MetroidGfx.NPC.SKREE_HIT), PlayMode.LOOP);
		animTimer = 0f;
		setRegion(spinAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(new SpriteFrameInput(position.cpy().add(SPECIAL_OFFSET)));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput.visible))
			return;
		animTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		switch(((SkreeSpriteFrameInput) frameInput).moveState) {
			case SLEEP:
			default:
				setRegion(spinAnim.getKeyFrame(animTimer));
				break;
			case FALL:
			case ONGROUND:
				setRegion(spinFastAnim.getKeyFrame(animTimer));
				break;
			case INJURY:
				setRegion(injuryAnim.getKeyFrame(animTimer));
				break;
			case DEAD:
				break;
		}
		postFrameInput(new SpriteFrameInput(frameInput.visible, frameInput.position.cpy().add(SPECIAL_OFFSET),
				false, frameInput.flipX, false));
	}
}
