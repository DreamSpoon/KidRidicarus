package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentSprite;
import kidridicarus.common.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.info.MetroidGfx;

public class MetroidDoorSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(48);
	private static final float ANIM_SPEED = 0.1f;
	private static final float ANIM_SPEED_CLOSE = 0.2f;

	private Animation<TextureRegion> closedAnim;
	private Animation<TextureRegion> closingAnim;
	private Animation<TextureRegion> openedAnim;
	private Animation<TextureRegion> openingAnim;
	private boolean isFacingRight;

	public MetroidDoorSprite(TextureAtlas atlas, Vector2 position, boolean isFacingRight) {
		super(true);
		closedAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.DOOR_CLOSED), PlayMode.LOOP);
		closingAnim = new Animation<TextureRegion>(ANIM_SPEED_CLOSE,
				atlas.findRegions(MetroidGfx.NPC.DOOR_CLOSING), PlayMode.NORMAL);
		openedAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.DOOR_OPENED), PlayMode.LOOP);
		openingAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.DOOR_OPENING), PlayMode.NORMAL);

		this.isFacingRight = isFacingRight;
		setRegion(closedAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		// if facing left then flip the texture region horizontally
		if(!isFacingRight && !isFlipX())
			flip(true, false);
	}

	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		MetroidDoorSpriteFrameInput myFrameInput = (MetroidDoorSpriteFrameInput) frameInput;
		isVisible = frameInput.visible;
		switch((myFrameInput).moveState) {
			case CLOSED:	// this must be funny to someone
			case OPENING_WAIT1:
				setRegion(closedAnim.getKeyFrame(myFrameInput.timeDelta));
				break;
			case OPENING_WAIT2:
				setRegion(openingAnim.getKeyFrame(myFrameInput.timeDelta));
				break;
			case OPEN:
				setRegion(openedAnim.getKeyFrame(myFrameInput.timeDelta));
				break;
			case CLOSING:
				setRegion(closingAnim.getKeyFrame(myFrameInput.timeDelta));
				break;
		}

		// if facing left then flip the texture region horizontally (if needed)
		if(!isFacingRight && !isFlipX())
			flip(true, false);
	}
}
