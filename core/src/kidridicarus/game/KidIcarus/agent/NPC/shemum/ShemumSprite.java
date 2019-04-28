package kidridicarus.game.KidIcarus.agent.NPC.shemum;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class ShemumSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2/15f;

	private Animation<TextureRegion> walkAnim;
	private float stateTimer;

	public ShemumSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(KidIcarusGfx.NPC.SHEMUM), PlayMode.LOOP);
		stateTimer = 0;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		setRegion(walkAnim.getKeyFrame(stateTimer));
		stateTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		applyFrameInput(frameInput);
	}
}
