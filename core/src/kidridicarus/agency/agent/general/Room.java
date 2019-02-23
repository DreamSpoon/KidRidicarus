package kidridicarus.agency.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.general.RoomBoxBody;
import kidridicarus.agency.info.UInfo;
import kidridicarus.game.info.KVInfo;

public class Room extends Agent {
	private RoomBoxBody rbody;
	private enum RoomType { CENTER, HSCROLL }
	private RoomType roomtype;
	private String roommusic;
	private float vOffset;

	public Room(Agency agency, AgentDef adef) {
		super(agency, adef);

		rbody = new RoomBoxBody(this, agency.getWorld(), adef.bounds);

		roomtype = RoomType.CENTER;
		String roomTypeStr = adef.properties.get(KVInfo.Room.KEY_ROOMTYPE, "", String.class);
		if(roomTypeStr.equals(KVInfo.Room.VAL_ROOMTYPE_HSCROLL))
			roomtype = RoomType.HSCROLL;
		else if(roomTypeStr.equals(KVInfo.Room.VAL_ROOMTYPE_CENTER))
			roomtype = RoomType.CENTER;

		// default to no music
		roommusic = "";
		if(adef.properties.containsKey(KVInfo.Room.KEY_ROOMMUSIC))
			roommusic = adef.properties.get(KVInfo.Room.KEY_ROOMMUSIC, String.class);

		vOffset = 0f;
		if(adef.properties.containsKey(KVInfo.Room.KEY_VIEWOFFSET_Y))
			vOffset = UInfo.P2M(Float.valueOf(adef.properties.get(KVInfo.Room.KEY_VIEWOFFSET_Y, String.class)));
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
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
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		rbody.dispose();
	}
}
