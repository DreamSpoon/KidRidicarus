package kidridicarus.game.KidIcarus.agent.NPC.shemum;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agentsprite.AnimSpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.KidIcarusGfx;

public class ShemumSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 2/15f;

	private Animation<TextureRegion> anim;
	private float animTimer;

	public ShemumSprite(TextureAtlas atlas, Vector2 position) {
		anim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(KidIcarusGfx.NPC.SHEMUM), PlayMode.LOOP);
		animTimer = 0;
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		applyFrameInput(new SpriteFrameInput(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		animTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		setRegion(anim.getKeyFrame(animTimer));
		applyFrameInput(frameInput);
	}
}
