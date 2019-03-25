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
	private RoomBoxBody rbody;
	private enum RoomType { CENTER, HSCROLL, VSCROLL }
	private RoomType roomtype;
	private String roommusic;
	private float viewVerticalOffset;
	private Direction4 viewScrollDir;

	public RoomBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		rbody = new RoomBoxBody(this, agency.getWorld(), Agent.getStartBounds(properties));

		roomtype = RoomType.CENTER;
		String roomTypeStr = properties.get(CommonKV.Room.KEY_ROOMTYPE, "", String.class);
		if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_HSCROLL))
			roomtype = RoomType.HSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_VSCROLL))
			roomtype = RoomType.VSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_CENTER))
			roomtype = RoomType.CENTER;
		roommusic = properties.get(CommonKV.Room.KEY_ROOMMUSIC, "", String.class);
		agency.getEar().registerMusic(roommusic);

		viewVerticalOffset = UInfo.P2M(properties.get(CommonKV.Room.KEY_VIEWOFFSET_Y, 0f, Float.class));
		viewScrollDir = Direction4.fromString(properties.get(CommonKV.Room.KEY_ROOM_SCROLL_DIR, "", String.class));
	}

	public Vector2 getViewCenterForPos(Vector2 playerPosition, Vector2 incomingPrevCenter) {
		Vector2 prevCenter;
		if(incomingPrevCenter == null)
			prevCenter = playerPosition.cpy();
		else
			prevCenter = incomingPrevCenter;

		Vector2 center = new Vector2();
		switch(roomtype) {
			case HSCROLL:
				// if allowed only scroll right then allow only increase x position
				if(viewScrollDir == Direction4.RIGHT)
					center.x = playerPosition.x > prevCenter.x ? playerPosition.x : prevCenter.x;
				// if allowed only scroll left then allow only decrease x position
				else if(viewScrollDir == Direction4.LEFT)
					center.x = playerPosition.x < prevCenter.x ? playerPosition.x : prevCenter.x;
				else
					center.x = playerPosition.x;

				center.y = rbody.getBounds().y + rbody.getBounds().height/2f + viewVerticalOffset;
				break;
			case VSCROLL:
				center.x = rbody.getBounds().x + rbody.getBounds().width/2f;

				// if allowed only scroll up then allow only increase y position
				if(viewScrollDir == Direction4.UP)
					center.y = playerPosition.y > prevCenter.y ? playerPosition.y : prevCenter.y;
				// if allowed only scroll left then allow only decrease x position
				else if(viewScrollDir == Direction4.DOWN)
					center.y = playerPosition.y < prevCenter.y ? playerPosition.y : prevCenter.y;
				else
					center.y = playerPosition.y;
				break;
			case CENTER:
			default:
				center.x = rbody.getBounds().x + rbody.getBounds().width/2f;
				center.y = rbody.getBounds().y + rbody.getBounds().height/2f + viewVerticalOffset;
				break;
		}
		return center;
	}

	public String getRoommusic() {
		return roommusic;
	}

	@Override
	public Vector2 getPosition() {
		return rbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return rbody.getBounds();
	}

	@Override
	public void disposeAgent() {
		rbody.dispose();
	}
}
