package com.ridicarus.kid.worldrunner;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;
import com.ridicarus.kid.GameInfo.Direction4;

/*
 * Eventually, spawnpoints might handle mario walking out of a tube, etc.
 * public enum SpawnpointType { IMMEDIATE, WALK_IN_FROM_LEFT, WALK_IN_FROM_RIGHT };
 * And have named spawnpoints for linking, anyways... for now, just the IMMEDIATE spawnpoint.
 */
public class Spawnpoint {
	private static final float SPAWN_SAFETYDIST = GameInfo.P2M(1);

	public enum SpawnType { IMMEDIATE, PIPEWARP };

	private Rectangle bounds;
	private String name;
	private boolean isMain;
	private SpawnType spawntype;
	private Direction4 direction;

	public Spawnpoint(MapObject object) {
		isMain = false;
		name = "";
		if(object.getProperties().containsKey(GameInfo.OBJKEY_SPAWNMAIN))
			isMain = true;
		else if(object.getProperties().containsKey(GameInfo.OBJKEY_NAME))
			name = object.getProperties().get(GameInfo.OBJKEY_NAME, String.class);

		// immediate is the default spawn case
		spawntype = SpawnType.IMMEDIATE;
		if(object.getProperties().containsKey(GameInfo.OBJKEY_SPAWNTYPE)) {
			String str = object.getProperties().get(GameInfo.OBJKEY_SPAWNTYPE, String.class);
			if(str.equals(GameInfo.OBJVAL_PIPESPAWN) &&
					object.getProperties().containsKey(GameInfo.OBJKEY_DIRECTION)) {
				spawntype = SpawnType.PIPEWARP;
				str = object.getProperties().get(GameInfo.OBJKEY_DIRECTION, String.class);
				if(str.equals(GameInfo.OBJVAL_RIGHT))
					direction = Direction4.RIGHT;
				else if(str.equals(GameInfo.OBJVAL_UP))
					direction = Direction4.UP;
				else if(str.equals(GameInfo.OBJVAL_LEFT))
					direction = Direction4.LEFT;
				else if(str.equals(GameInfo.OBJVAL_DOWN))
					direction = Direction4.DOWN;
			}
		}

		// convert bounds to in game units
		Rectangle b = ((RectangleMapObject) object).getRectangle();
		bounds = new Rectangle(GameInfo.P2M(b.x), GameInfo.P2M(b.y), GameInfo.P2M(b.width), GameInfo.P2M(b.height));
	}

	public boolean isMainSpawn() {
		return isMain;
	}

	public String getName() {
		return name;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public Vector2 getCenter() {
		return bounds.getCenter(new Vector2());
	}

	public SpawnType getSpawnType() {
		return spawntype;
	}

	public Direction4 getDirection() {
		return direction;
	}

	public Vector2 calcBeginOffsetFromSpawn(Vector2 playerSize) {
		switch(direction) {
			case RIGHT:
				return new Vector2(bounds.x + bounds.width + playerSize.x/2f + SPAWN_SAFETYDIST,
						bounds.y + playerSize.y/2f);
			case UP:
				return new Vector2(bounds.x + bounds.width/2f,
						bounds.y + bounds.height + playerSize.y/2f + SPAWN_SAFETYDIST);
			case LEFT:
				return new Vector2(bounds.x - playerSize.x/2f - SPAWN_SAFETYDIST,
						bounds.y + playerSize.y/2f);
			case DOWN:
			default:
				return new Vector2(bounds.x + bounds.width/2f,
						bounds.y - playerSize.y/2f - SPAWN_SAFETYDIST);
		}
	}
}
