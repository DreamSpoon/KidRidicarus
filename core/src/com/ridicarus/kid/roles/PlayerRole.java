package com.ridicarus.kid.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.ridicarus.kid.GameInfo.SpriteDrawOrder;
import com.ridicarus.kid.tools.BasicInputs;
import com.ridicarus.kid.tools.Spawnpoint;

public interface PlayerRole {
	public void update(float delta, BasicInputs bi);
	public void draw(Batch batch);
	public SpriteDrawOrder getDrawOrder();

	public Vector2 getPosition(); 
	public boolean isDead();
	public float getStateTimer();

	public Spawnpoint getWarpSpawnpoint();
	public void respawn(Spawnpoint sp);
	public boolean isAtLevelEnd();
}
