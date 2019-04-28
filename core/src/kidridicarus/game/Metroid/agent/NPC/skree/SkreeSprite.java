package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.MetroidGfx;

public class SkreeSprite extends AgentSprite {
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
		super(true);
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

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		switch(((SkreeSpriteFrameInput) frameInput).moveState) {
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
		stateTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		applyFrameInput(new SpriteFrameInput(frameInput.visible,
				new Vector2(frameInput.position.x - getWidth()/2f + SPECIAL_OFFSET.x,
				frameInput.position.y - getHeight()/2f + SPECIAL_OFFSET.y), frameInput.flipX));
	}
}
