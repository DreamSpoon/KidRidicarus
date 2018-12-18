package kidridicarus.roles.robot.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.bodies.general.PlayerSpawnerBody;
import kidridicarus.info.KVInfo;
import kidridicarus.info.UInfo;
import kidridicarus.info.GameInfo.Direction4;
import kidridicarus.roles.RobotRole;
import kidridicarus.worldrunner.RobotRoleDef;
import kidridicarus.worldrunner.RoleWorld;

public class PlayerSpawner implements RobotRole {
	private static final float SPAWN_SAFETYDIST = UInfo.P2M(1);

	public enum SpawnType { IMMEDIATE, PIPEWARP };

	private MapProperties properties;
	private PlayerSpawnerBody psbody;

	private String name;
	private boolean isMain;
	private SpawnType spawntype;
	private Direction4 direction;

	public PlayerSpawner(RoleWorld roleWorld, RobotRoleDef rdef) {
		properties = rdef.properties;
		isMain = false;
		name = "";
		if(rdef.properties.containsKey(KVInfo.KEY_SPAWNMAIN))
			isMain = true;
		else if(rdef.properties.containsKey(KVInfo.KEY_NAME))
			name = rdef.properties.get(KVInfo.KEY_NAME, String.class);

		// immediate is the default spawn case
		spawntype = SpawnType.IMMEDIATE;
		if(rdef.properties.containsKey(KVInfo.KEY_SPAWNTYPE)) {
			String str = rdef.properties.get(KVInfo.KEY_SPAWNTYPE, String.class);
			if(str.equals(KVInfo.VAL_PIPEWARP) &&
					rdef.properties.containsKey(KVInfo.KEY_DIRECTION)) {
				spawntype = SpawnType.PIPEWARP;
				str = rdef.properties.get(KVInfo.KEY_DIRECTION, String.class);
				if(str.equals(KVInfo.VAL_RIGHT))
					direction = Direction4.RIGHT;
				else if(str.equals(KVInfo.VAL_UP))
					direction = Direction4.UP;
				else if(str.equals(KVInfo.VAL_LEFT))
					direction = Direction4.LEFT;
				else if(str.equals(KVInfo.VAL_DOWN))
					direction = Direction4.DOWN;
			}
		}

		psbody = new PlayerSpawnerBody(roleWorld.getWorld(), this, rdef.bounds);
	}

	public Vector2 calcBeginOffsetFromSpawn(Vector2 playerSize) {
		switch(direction) {
			case RIGHT:
				return new Vector2(psbody.getBounds().x + psbody.getBounds().width +
						playerSize.x/2f + SPAWN_SAFETYDIST, psbody.getBounds().y + playerSize.y/2f);
			case UP:
				return new Vector2(psbody.getBounds().x + psbody.getBounds().width/2f,
						psbody.getBounds().y + psbody.getBounds().height + playerSize.y/2f + SPAWN_SAFETYDIST);
			case LEFT:
				return new Vector2(psbody.getBounds().x - playerSize.x/2f - SPAWN_SAFETYDIST,
						psbody.getBounds().y + playerSize.y/2f);
			case DOWN:
			default:
				return new Vector2(psbody.getBounds().x + psbody.getBounds().width/2f,
						psbody.getBounds().y - playerSize.y/2f - SPAWN_SAFETYDIST);
		}
	}

	public boolean isMainSpawn() {
		return isMain;
	}

	public String getName() {
		return name;
	}

	public SpawnType getSpawnType() {
		return spawntype;
	}

	public Direction4 getDirection() {
		return direction;
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return psbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return psbody.getBounds();
	}

	@Override
	public MapProperties getProperties() {
		return properties;
	}

	@Override
	public void dispose() {
		psbody.dispose();
	}
}
