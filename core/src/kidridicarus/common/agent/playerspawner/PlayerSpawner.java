package kidridicarus.common.agent.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.common.tool.Direction4;

public class PlayerSpawner extends PlacedBoundsAgent implements DisposableAgent {
	private enum SpawnType { IMMEDIATE, PIPEWARP }

	private PlayerSpawnerBody psbody;

	private boolean isMain;
	private SpawnType spawntype;
	private Direction4 direction;

	public PlayerSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isMain = properties.containsKey(CommonKV.Spawn.KEY_SPAWN_MAIN);

		// immediate is the default spawn case
		spawntype = SpawnType.IMMEDIATE;
		String str = properties.get(CommonKV.Spawn.KEY_SPAWN_SCRIPT, "", String.class);
		if(str.equals(CommonKV.Spawn.VAL_SPAWN_SCRIPT_PIPEWARP) && properties.containsKey(CommonKV.KEY_DIRECTION)) {
			spawntype = SpawnType.PIPEWARP;
			direction = Direction4.fromString(properties.get(CommonKV.KEY_DIRECTION, "", String.class));
		}

		psbody = new PlayerSpawnerBody(agency.getWorld(), this, AP_Tool.getBounds(properties));
	}

	public boolean isMainSpawn() {
		return isMain;
	}

	public SpawnType getSpawnType() {
		return spawntype;
	}

	public Direction4 getDirection() {
		return direction;
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
	public void disposeAgent() {
		psbody.dispose();
	}
}
