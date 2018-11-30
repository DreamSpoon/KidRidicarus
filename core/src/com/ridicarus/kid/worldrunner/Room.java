package com.ridicarus.kid.worldrunner;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.roles.PlayerRole;

public class Room {
	private Rectangle bounds;
	private enum RoomType { CENTER, HSCROLL };
	private RoomType roomtype;
	private String roommusic;

	public Room(WorldRunner worldRunner, MapObject object) {
		Rectangle rect = ((RectangleMapObject) object).getRectangle();
		bounds = new Rectangle(GameInfo.P2M(rect.x), GameInfo.P2M(rect.y),
				GameInfo.P2M(rect.width), GameInfo.P2M(rect.height));
		// default to CENTER roomtype
		roomtype = RoomType.CENTER;
		MapProperties p = object.getProperties();
		if(p.containsKey(GameInfo.OBJKEY_ROOMTYPE)) {
			if(p.get(GameInfo.OBJKEY_ROOMTYPE, String.class).equals(GameInfo.OBJVAL_ROOMTYPE_HSCROLL))
				roomtype = RoomType.HSCROLL;
		}
		// default to no music
		roommusic = "";
		if(p.containsKey(GameInfo.OBJKEY_ROOMMUSIC)) {
			roommusic = p.get(GameInfo.OBJKEY_ROOMMUSIC, String.class);
		}
	}

	// simple point form bounds check 
	public boolean isPlayerInRoom(PlayerRole pr) {
		return bounds.contains(pr.getPosition());
	}

	public void setGamecamPosition(OrthographicCamera gamecam, PlayerRole pr) {
		switch(roomtype) {
			case HSCROLL:
				gamecam.position.x = pr.getPosition().x;
				gamecam.position.y = bounds.y + bounds.height/2f;
				break;
			case CENTER:
			default:
				gamecam.position.x = bounds.x + bounds.width/2f;
				gamecam.position.y = bounds.y + bounds.height/2f;
				break;
		}
		gamecam.update();
	}

	public String getRoommusic() {
		return roommusic;
	}
}
