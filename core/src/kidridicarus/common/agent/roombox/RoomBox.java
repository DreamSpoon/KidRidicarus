package kidridicarus.common.agent.roombox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
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
				center = getHscrollCenter(playerPosition, prevCenter);
				break;
			case VSCROLL:
				center = getVscrollCenter(playerPosition, prevCenter);
				break;
			case CENTER:
			default:
				center = getCenterCenter();
				break;
		}
		return center;
	}

	private Vector2 getHscrollCenter(Vector2 playerPosition, Vector2 prevCenter) {
		Vector2 nextCenter = playerPosition.cpy();
		// if allowed only scroll right then allow only increase x position
		if(viewScrollDir == Direction4.RIGHT)
			nextCenter.x = playerPosition.x > prevCenter.x ? playerPosition.x : prevCenter.x;
		// if allowed only scroll left then allow only decrease x position
		else if(viewScrollDir == Direction4.LEFT)
			nextCenter.x = playerPosition.x < prevCenter.x ? playerPosition.x : prevCenter.x;
		else
			nextCenter.x = playerPosition.x;

		// minX = left bound of room + half screen width
		float minX = body.getBounds().x + UInfo.P2M(CommonInfo.V_WIDTH)/2f;
		if(nextCenter.x < minX)
			nextCenter.x = minX;
		// maxX = right bound of room - half screen width
		float maxX = body.getBounds().x + body.getBounds().width - UInfo.P2M(CommonInfo.V_WIDTH)/2f;
		if(nextCenter.x > maxX)
			nextCenter.x = maxX;

		nextCenter.y = body.getBounds().y + body.getBounds().height/2f + viewVerticalOffset;

		return nextCenter;
	}

	private Vector2 getVscrollCenter(Vector2 playerPosition, Vector2 prevCenter) {
		Vector2 nextCenter = playerPosition.cpy();

		nextCenter.x = body.getBounds().x + body.getBounds().width/2f;

		// if allowed only scroll up then allow only increase y position
		if(viewScrollDir == Direction4.UP)
			nextCenter.y = playerPosition.y > prevCenter.y ? playerPosition.y : prevCenter.y;
		// if allowed only scroll left then allow only decrease x position
		else if(viewScrollDir == Direction4.DOWN)
			nextCenter.y = playerPosition.y < prevCenter.y ? playerPosition.y : prevCenter.y;
		else
			nextCenter.y = playerPosition.y;

		return nextCenter;
	}

	private Vector2 getCenterCenter() {
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
