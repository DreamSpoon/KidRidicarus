package kidridicarus.common.agent.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

/*
 * A box with properties applicable to a space, whose properties can be queried.
 * e.g. Create a room and whenever the player is contacting the room, the player can query the room for the
 * current room music. Also applicable to viewpoint, since the room can specify which way the screen scrolls
 * and if the view should be offset.
 */
public class RoomBox extends PlacedBoundsAgent implements DisposableAgent {
	private RoomBoxBody body;
	private enum RoomType { CENTER, HSCROLL, VSCROLL }
	private RoomType roomType;
	private String roomMusicStr;
	private float viewVerticalOffset;
	private Direction4 viewScrollDir;
	private Float scrollVelocity;
	private boolean isScrollBoundX;
	private boolean isScrollBoundY;

	public RoomBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		body = new RoomBoxBody(this, agency.getWorld(), AP_Tool.getBounds(properties));

		roomType = RoomType.CENTER;
		String roomTypeStr = properties.get(CommonKV.Room.KEY_TYPE, "", String.class);
		if(roomTypeStr.equals(CommonKV.Room.VAL_TYPE_SCROLL_X))
			roomType = RoomType.HSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_TYPE_SCROLL_Y))
			roomType = RoomType.VSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_TYPE_CENTER))
			roomType = RoomType.CENTER;
		roomMusicStr = properties.get(CommonKV.Room.KEY_MUSIC, "", String.class);
		agency.getEar().registerMusic(roomMusicStr);

		viewVerticalOffset = UInfo.P2M(properties.get(CommonKV.Room.KEY_VIEWOFFSET_Y, 0f, Float.class));
		viewScrollDir = Direction4.fromString(properties.get(CommonKV.Room.KEY_SCROLL_DIR, "", String.class));
		scrollVelocity = properties.get(CommonKV.Room.KEY_SCROLL_VEL, null, Float.class);
		if(scrollVelocity != null)
			scrollVelocity = UInfo.P2M(scrollVelocity);
		isScrollBoundX = properties.get(CommonKV.Room.KEY_SCROLL_BOUND_X, false, Boolean.class);
		isScrollBoundY = properties.get(CommonKV.Room.KEY_SCROLL_BOUND_Y, false, Boolean.class);
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
				center = getScrollViewCenter(playerPosition, prevCenter, true);
				break;
			case VSCROLL:
				center = getScrollViewCenter(playerPosition, prevCenter, false);
				break;
			case CENTER:
			default:
				center = getCenterViewCenter();
				break;
		}
		return center;
	}

	private Vector2 getScrollViewCenter(Vector2 playerPosition, Vector2 prevCenter, boolean isScrollH) {
		// return previous center as default if needed
		Vector2 nextCenter = null;
		Vector2 safePrevCenter = prevCenter == null ? playerPosition.cpy() : prevCenter.cpy();
		// if scrolling horizontally then check/do view center move, cap velocity, and apply offset
		if(viewScrollDir.isHorizontal()) {
			float moveX = playerPosition.x - safePrevCenter.x;
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
			nextCenter = new Vector2(safePrevCenter.x+moveX,
					body.getBounds().y + body.getBounds().height/2f + viewVerticalOffset);
		}
		// if scrolling vertically then check/do view center move, cap velocity
		else if(viewScrollDir.isVertical()) {
			float moveY = playerPosition.y - safePrevCenter.y;
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
			// TODO nextCenter.x += viewHorizontalOffset;
			nextCenter = new Vector2(body.getBounds().x + body.getBounds().width/2f, safePrevCenter.y+moveY);
		}
		else {
			if(isScrollH) {
				nextCenter = new Vector2(playerPosition.x,
						body.getBounds().y + body.getBounds().height/2f + viewVerticalOffset);
			}
			else
				nextCenter = new Vector2(body.getBounds().x + body.getBounds().width/2f, playerPosition.y);
		}

		if(isScrollBoundX) {
			// minX = far left of room plus half of screen width
			float minX = body.getBounds().x + UInfo.P2M(CommonInfo.V_WIDTH)/2f;
			// maxX = far right of room minus half of screen width
			float maxX = body.getBounds().x + body.getBounds().width - UInfo.P2M(CommonInfo.V_WIDTH)/2f;
			if(nextCenter.x < minX)
				nextCenter.x = minX;
			else if(nextCenter.x > maxX)
				nextCenter.x = maxX;
		}
		if(isScrollBoundY) {
			// minY = very bottom of room plus half of screen height
			float minY = body.getBounds().y + UInfo.P2M(CommonInfo.V_HEIGHT)/2f;
			// maxY = very top of room minus half of screen height
			float maxY = body.getBounds().y + body.getBounds().height - UInfo.P2M(CommonInfo.V_HEIGHT)/2f;
			if(nextCenter.y < minY)
				nextCenter.y = minY;
			else if(nextCenter.y > maxY)
				nextCenter.y = maxY;
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
