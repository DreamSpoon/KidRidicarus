package kidridicarus.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.bodies.general.RoomBoxBody;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;

public class Room extends Agent {
	private RoomBoxBody rbody;
	private enum RoomType { CENTER, HSCROLL };
	private RoomType roomtype;
	private String roommusic;
	private float vOffset;

	public Room(Agency agency, AgentDef adef) {
		super(agency, adef);

		rbody = new RoomBoxBody(this, agency.getWorld(), adef.bounds);

		// default to CENTER roomtype
		roomtype = RoomType.CENTER;
		if(adef.properties.containsKey(KVInfo.KEY_ROOMTYPE)) {
			if(adef.properties.get(KVInfo.KEY_ROOMTYPE, String.class).equals(KVInfo.VAL_ROOMTYPE_HSCROLL))
				roomtype = RoomType.HSCROLL;
		}
		// default to no music
		roommusic = "";
		if(adef.properties.containsKey(KVInfo.KEY_ROOMMUSIC))
			roommusic = adef.properties.get(KVInfo.KEY_ROOMMUSIC, String.class);

		vOffset = 0f;
		if(adef.properties.containsKey(KVInfo.KEY_VIEWOFFSET_Y))
			vOffset = UInfo.P2M(Float.valueOf(adef.properties.get(KVInfo.KEY_VIEWOFFSET_Y, String.class)));
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

	public boolean isPointInRoom(Vector2 position) {
		return rbody.getBounds().contains(position);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
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
	public void dispose() {
		rbody.dispose();
	}
}
