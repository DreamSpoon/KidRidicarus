package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import kidridicarus.agency.agentsprite.AgentSprite;
import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.info.UInfo;
import kidridicarus.game.Metroid.MetroidGfx;

class MetroidDoorSprite extends AgentSprite {
	private static final float SPRITE_WIDTH = UInfo.P2M(8);
	private static final float SPRITE_HEIGHT = UInfo.P2M(48);
	private static final float ANIM_SPEED = 0.1f;
	private static final float ANIM_SPEED_CLOSE = 0.2f;

	private Animation<TextureRegion> closedAnim;
	private Animation<TextureRegion> closingAnim;
	private Animation<TextureRegion> openedAnim;
	private Animation<TextureRegion> openingAnim;

	MetroidDoorSprite(TextureAtlas atlas, SpriteFrameInput frameInput) {
		closedAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.DOOR_CLOSED), PlayMode.LOOP);
		closingAnim = new Animation<TextureRegion>(ANIM_SPEED_CLOSE,
				atlas.findRegions(MetroidGfx.NPC.DOOR_CLOSING), PlayMode.NORMAL);
		openedAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.DOOR_OPENED), PlayMode.LOOP);
		openingAnim = new Animation<TextureRegion>(ANIM_SPEED,
				atlas.findRegions(MetroidGfx.NPC.DOOR_OPENING), PlayMode.NORMAL);
		setRegion(closedAnim.getKeyFrame(0f));
		setBounds(getX(), getY(), SPRITE_WIDTH, SPRITE_HEIGHT);
		postFrameInput(frameInput);
	}

	// use the absolute time to get the animation key frame
	@Override
	public void processFrame(SpriteFrameInput frameInput) {
		if(!preFrameInput(frameInput))
			return;
		switch(((MetroidDoorSpriteFrameInput) frameInput).moveState) {
			case CLOSED:	// this must be funny to someone
			case OPENING_WAIT1:
				setRegion(closedAnim.getKeyFrame(frameInput.frameTime.timeAbs));
				break;
			case OPENING_WAIT2:
				setRegion(openingAnim.getKeyFrame(frameInput.frameTime.timeAbs));
				break;
			case OPEN:
				setRegion(openedAnim.getKeyFrame(frameInput.frameTime.timeAbs));
				break;
			case CLOSING:
				setRegion(closingAnim.getKeyFrame(frameInput.frameTime.timeAbs));
				break;
		}
		postFrameInput(frameInput);
	}
}
