package kidridicarus.game.agent.sprite.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.info.UInfo;
import kidridicarus.game.agent.Metroid.NPC.MetroidDoor.MoveState;
import kidridicarus.game.info.MetroidAnim;

public class MetroidDoorSprite extends Sprite {
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
		closedAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.NPC.DOOR_CLOSED), PlayMode.LOOP);
		closingAnim = new Animation<TextureRegion>(ANIM_SPEED_CLOSE,
				atlas.findRegions(MetroidAnim.NPC.DOOR_CLOSING), PlayMode.NORMAL);
		openedAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.NPC.DOOR_OPENED), PlayMode.LOOP);
		openingAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidAnim.NPC.DOOR_OPENING), PlayMode.NORMAL);

		this.isFacingRight = isFacingRight;
		setRegion(closedAnim.getKeyFrame(0));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		setPosition(position.x - getWidth()/2f, position.y - getHeight()/2f);
		// if facing left then flip the texture region horizontally
		if(!isFacingRight)
			flip(true, false);
	}

	public void update(float stateTimer, MoveState moveState) {
		switch(moveState) {
			case CLOSED:	// this must be funny to someone
			case OPENING_WAIT1:
				setRegion(closedAnim.getKeyFrame(stateTimer));
				break;
			case OPENING_WAIT2:
				setRegion(openingAnim.getKeyFrame(stateTimer));
				break;
			case OPEN:
				setRegion(openedAnim.getKeyFrame(stateTimer));
				break;
			case CLOSING:
				setRegion(closingAnim.getKeyFrame(stateTimer));
				break;
		}

		// if facing left then flip the texture region horizontally (if needed)
		if(!isFacingRight && !isFlipX())
			flip(true, false);
	}
}
