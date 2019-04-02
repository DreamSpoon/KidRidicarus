package kidridicarus.common.agent.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.Direction4;

/*
 * A box with properties applicable to a space, whose properties can be queried.
 * e.g. Create a room and whenever the player is contacting the room, the player can query the room for the
 * current room music. Also applicable to viewpoint, since the room can specify which way the screen scrolls
 * and if the view should be offset.
 */
public class RoomBox extends Agent implements DisposableAgent {
	private RoomBoxBody body;
	private enum RoomType { CENTER, HSCROLL, VSCROLL }
	private RoomType roomType;
	private String roomMusicStr;
	private float viewVerticalOffset;
	private Direction4 viewScrollDir;
	private Float scrollVelocity;

	public RoomBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		body = new RoomBoxBody(this, agency.getWorld(), Agent.getStartBounds(properties));

		roomType = RoomType.CENTER;
		String roomTypeStr = properties.get(CommonKV.Room.KEY_ROOMTYPE, "", String.class);
		if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_HSCROLL))
			roomType = RoomType.HSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_VSCROLL))
			roomType = RoomType.VSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_CENTER))
			roomType = RoomType.CENTER;
		roomMusicStr = properties.get(CommonKV.Room.KEY_ROOMMUSIC, "", String.class);
		agency.getEar().registerMusic(roomMusicStr);

		viewVerticalOffset = UInfo.P2M(properties.get(CommonKV.Room.KEY_VIEWOFFSET_Y, 0f, Float.class));
		viewScrollDir = Direction4.fromString(properties.get(CommonKV.Room.KEY_ROOM_SCROLL_DIR, "", String.class));
		scrollVelocity = properties.get(CommonKV.Room.KEY_ROOM_SCROLL_VELOCITY, null, Float.class);
		if(scrollVelocity != null)
			scrollVelocity = UInfo.P2M(scrollVelocity);
	}

	public Vector2 getViewCenterForPos(Vector2 playerPosition, Vector2 incomingPrevCenter) {
		Vector2 prevCenter;
		if(incomingPrevCenter == null)
			prevCenter = playerPosition.cpy();
		else
			prevCenter = incomingPrevCenter;

		Vector2 center;
		switch(roomType) {
			case HSCROLL:
				center = getScrollViewCenter(playerPosition, prevCenter);
				break;
			case VSCROLL:
				center = getScrollViewCenter(playerPosition, prevCenter);
				break;
			case CENTER:
			default:
				center = getCenterViewCenter();
				break;
		}
		return center;
	}

	private Vector2 getScrollViewCenter(Vector2 playerPosition, Vector2 prevCenter) {
		// return previous center as default if needed
		Vector2 nextCenter = prevCenter == null ? null : prevCenter.cpy();

		// if scrolling horizontally then check/do view center move, cap velocity, and apply offset
		if(viewScrollDir.isHorizontal()) {
			float moveX = playerPosition.x - prevCenter.x;
			if(viewScrollDir == Direction4.RIGHT) {
				if(moveX < 0f)
					moveX = 0f;
				else if(scrollVelocity != null && moveX > scrollVelocity)
					moveX = scrollVelocity;
			}
			else if(viewScrollDir == Direction4.LEFT) {
				if(moveX > 0f)
					moveX = 0f;
				else if(scrollVelocity != null && moveX < -scrollVelocity)
					moveX = -scrollVelocity;
			}
			nextCenter = new Vector2(prevCenter.x+moveX,
					body.getBounds().y + body.getBounds().height/2f + viewVerticalOffset);
		}
		// if scrolling vertically then check/do view center move, cap velocity
		else if(viewScrollDir.isVertical()) {
			float moveY = playerPosition.y - prevCenter.y;
			if(viewScrollDir == Direction4.UP) {
				if(moveY < 0f)
					moveY = 0f;
				else if(scrollVelocity != null && moveY > scrollVelocity)
					moveY = scrollVelocity;
			}
			else if(viewScrollDir == Direction4.DOWN) {
				if(moveY > 0f)
					moveY = 0f;
				else if(scrollVelocity != null && moveY < -scrollVelocity)
					moveY = -scrollVelocity;
			}
			// TODO nexCener.y += viewHorizontalOffset;
			nextCenter = new Vector2(body.getBounds().x + body.getBounds().width/2f, prevCenter.y+moveY);
		}

		return nextCenter;
	}

	private Vector2 getCenterViewCenter() {
		return new Vector2(body.getBounds().x + body.getBounds().width/2f,
				body.getBounds().y + body.getBounds().height/2f + viewVerticalOffset);
	}

	public String getRoommusic() {
		return roomMusicStr;
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
