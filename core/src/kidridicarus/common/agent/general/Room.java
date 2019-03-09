package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agentbody.general.RoomBoxBody;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.UInfo;

/*
 * A box with properties applicable to a space, whose properties can be queried.
 * e.g. Create a room and whenever the player is contacting the room, the player can query the room for the
 * current room music. Also applicable to viewpoint, since the room can specify which way the screen scrolls
 * and if the view should be offset.
 */
public class Room extends Agent implements DisposableAgent {
	private RoomBoxBody rbody;
	private enum RoomType { CENTER, HSCROLL }
	private RoomType roomtype;
	private String roommusic;
	private float vOffset;

	public Room(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		rbody = new RoomBoxBody(this, agency.getWorld(), Agent.getStartBounds(properties));

		roomtype = RoomType.CENTER;
		String roomTypeStr = properties.get(CommonKV.Room.KEY_ROOMTYPE, "", String.class);
		if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_HSCROLL))
			roomtype = RoomType.HSCROLL;
		else if(roomTypeStr.equals(CommonKV.Room.VAL_ROOMTYPE_CENTER))
			roomtype = RoomType.CENTER;

		roommusic = properties.get(CommonKV.Room.KEY_ROOMMUSIC, "", String.class);
		agency.registerMusic(roommusic);

		vOffset = UInfo.P2M(properties.get(CommonKV.Room.KEY_VIEWOFFSET_Y, 0f, Float.class));
	}

	public Vector2 getViewCenterForPos(Vector2 pos) {
		Vector2 center = new Vector2();
		switch(roomtype) {
			case HSCROLL:
				center.x = pos.x;
				center.y = rbody.getBounds().y + rbody.getBounds().height/2f + vOffset;
				break;
			case CENTER:
			default:
				center.x = rbody.getBounds().x + rbody.getBounds().width/2f;
				center.y = rbody.getBounds().y + rbody.getBounds().height/2f + vOffset;
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
