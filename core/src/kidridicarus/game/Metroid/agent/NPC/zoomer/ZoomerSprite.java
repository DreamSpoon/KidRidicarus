package kidridicarus.game.Metroid.agent.NPC.zoomer;

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

public class ZoomerSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.05f;

	private Animation<TextureRegion> walkAnim;
	private Animation<TextureRegion> injuryAnim;
	private float animTimer;

	public ZoomerSprite(TextureAtlas atlas, Vector2 position) {
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.ZOOMER), PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.ZOOMER_HIT), PlayMode.LOOP);
		animTimer = 0f;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		postFrameInput(new SpriteFrameInput(position));
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput.visible))
			return;
		animTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		// set region according to move state
		switch(((ZoomerSpriteFrameInput) frameInput).moveState) {
			case WALK:
				setRegion(walkAnim.getKeyFrame(animTimer));
				break;
			case INJURY:
				setRegion(injuryAnim.getKeyFrame(animTimer));
				break;
			case DEAD:
				break;
		}
		// rotate sprite according to up direction
		switch(((ZoomerSpriteFrameInput) frameInput).upDir) {
			case RIGHT:
				setRotation(270);
				break;
			case UP:
				setRotation(0);
				break;
			case LEFT:
				setRotation(90);
				break;
			case DOWN:
			default:
				setRotation(180);
				break;
		}
		postFrameInput(frameInput);
	}
}
