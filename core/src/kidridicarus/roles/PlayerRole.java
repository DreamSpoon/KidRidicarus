package kidridicarus.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.GameInfo.SpriteDrawOrder;
import kidridicarus.tools.BasicInputs;
import kidridicarus.worldrunner.Spawnpoint;

public interface PlayerRole extends Disposable {
	public void update(float delta, BasicInputs bi);
	public void draw(Batch batch);
	public SpriteDrawOrder getDrawOrder();

	public Vector2 getPosition(); 
	public boolean isDead();
	public float getStateTimer();

	public Spawnpoint getWarpSpawnpoint();
	public void respawn(Spawnpoint sp);
	public boolean isAtLevelEnd();
	public boolean isOnGround();
}
