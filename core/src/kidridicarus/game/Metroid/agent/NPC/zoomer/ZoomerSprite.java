package kidridicarus.game.Metroid.agent.NPC.zoomer;

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

public class ZoomerSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(16);
	private static final float SPRITE_HEIGHT = UInfo.P2M(16);
	private static final float ANIM_SPEED = 0.05f;

	private Animation<TextureRegion> walkAnim;
	private Animation<TextureRegion> injuryAnim;
	private float stateTimer;

	public ZoomerSprite(TextureAtlas atlas, Vector2 position) {
		super(true);
		walkAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.ZOOMER), PlayMode.LOOP);
		injuryAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.ZOOMER_HIT), PlayMode.LOOP);
		stateTimer = 0;
		setRegion(walkAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setOrigin(SPRITE_WIDTH/2f, SPRITE_HEIGHT/2f);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		// set region according to move state
		switch(((ZoomerSpriteFrameInput) frameInput).moveState) {
			case WALK:
				setRegion(walkAnim.getKeyFrame(stateTimer));
				break;
			case INJURY:
				setRegion(injuryAnim.getKeyFrame(stateTimer));
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
		stateTimer += ((AnimSpriteFrameInput) frameInput).timeDelta;
		applyFrameInput(frameInput);
	}
}
