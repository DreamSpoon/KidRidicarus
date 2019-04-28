package kidridicarus.game.Metroid.agent.item.marumari;

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

public class MaruMariSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.033f;

	private Animation<TextureRegion> anim;
	private float animTimer;

	public MaruMariSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		anim = new Animation<TextureRegion>(ANIM_SPEED, atlas.findRegions(MetroidGfx.Item.MARUMARI), PlayMode.LOOP);
		setRegion(anim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		setRegion(anim.getKeyFrame(animTimer));
		animTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		applyFrameInput(frameInput);
	}
}
