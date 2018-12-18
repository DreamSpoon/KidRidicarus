package kidridicarus.roles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.roles.robot.general.PlayerSpawner;
import kidridicarus.roles.robot.general.Room;
import kidridicarus.tools.BasicInputs;

public interface PlayerRole extends Disposable {
	public void update(float delta, BasicInputs bi);
	public void draw(Batch batch);
	public SpriteDrawOrder getDrawOrder();

	public Vector2 getPosition(); 
	public boolean isDead();
	public float getStateTimer();

	public PlayerSpawner getWarpSpawnpoint();
	public void respawn(PlayerSpawner sp);
	public boolean isAtLevelEnd();
	public boolean isOnGround();

	public Room getCurrentRoom();
}
