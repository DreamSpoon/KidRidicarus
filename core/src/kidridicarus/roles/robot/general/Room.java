package kidridicarus.roles.robot.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.general.RoomBoxBody;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class Room implements RobotRole {
	private MapProperties properties;
	private RoomBoxBody rbody;
	private enum RoomType { CENTER, HSCROLL };
	private RoomType roomtype;
	private String roommusic;
	private float vOffset;

	public Room(RoleWorld runner, RobotRoleDef rdef) {
		properties = rdef.properties;
		rbody = new RoomBoxBody(this, runner.getWorld(), rdef.bounds);

		// default to CENTER roomtype
		roomtype = RoomType.CENTER;
		if(rdef.properties.containsKey(KVInfo.KEY_ROOMTYPE)) {
			if(rdef.properties.get(KVInfo.KEY_ROOMTYPE, String.class).equals(KVInfo.VAL_ROOMTYPE_HSCROLL))
				roomtype = RoomType.HSCROLL;
		}
		// default to no music
		roommusic = "";
		if(rdef.properties.containsKey(KVInfo.KEY_ROOMMUSIC))
			roommusic = rdef.properties.get(KVInfo.KEY_ROOMMUSIC, String.class);

		vOffset = 0f;
		if(rdef.properties.containsKey(KVInfo.KEY_VIEWOFFSET_Y))
			vOffset = UInfo.P2M(Float.valueOf(rdef.properties.get(KVInfo.KEY_VIEWOFFSET_Y, String.class)));
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
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		rbody.dispose();
	}
}
