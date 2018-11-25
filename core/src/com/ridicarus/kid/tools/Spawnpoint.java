package com.ridicarus.kid.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo;

/*
 * Eventually, spawnpoints might handle mario walking out of a tube, etc.
 * public enum SpawnpointType { IMMEDIATE, WALK_IN_FROM_LEFT, WALK_IN_FROM_RIGHT };
 * And have named spawnpoints for linking, anyways... for now, just the IMMEDIATE spawnpoint.
 */
public class Spawnpoint {
	public Vector2 position;

	public Spawnpoint(MapObject object) {
		Rectangle bounds = ((RectangleMapObject) object).getRectangle();
		position = new Vector2(GameInfo.P2M(bounds.getX() + bounds.getWidth() / 2f),
				GameInfo.P2M(bounds.getY() + bounds.getHeight() / 2f));
	}
}
