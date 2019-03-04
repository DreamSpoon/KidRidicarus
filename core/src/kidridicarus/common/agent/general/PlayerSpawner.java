package kidridicarus.common.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.Direction4;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agentbody.general.PlayerSpawnerBody;
import kidridicarus.agency.info.AgencyKV;

public class PlayerSpawner extends Agent {
	private static final float SPAWN_SAFETYDIST = UInfo.P2M(1);

	public enum SpawnType { IMMEDIATE, PIPEWARP }

	private PlayerSpawnerBody psbody;

	private boolean isMain;
	private SpawnType spawntype;
	private Direction4 direction;

	public PlayerSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		isMain = properties.containsKey(AgencyKV.Spawn.KEY_SPAWNMAIN);

		// immediate is the default spawn case
		spawntype = SpawnType.IMMEDIATE;
		if(properties.containsKey(AgencyKV.Spawn.KEY_SPAWNTYPE)) {
			String str = properties.get(AgencyKV.Spawn.KEY_SPAWNTYPE, "", String.class);
			if(str.equals(AgencyKV.Spawn.VAL_PIPEWARP_SPAWN) &&
					properties.containsKey(AgencyKV.KEY_DIRECTION)) {
				spawntype = SpawnType.PIPEWARP;
				direction = Direction4.fromString(properties.get(AgencyKV.KEY_DIRECTION, "", String.class));
			}
		}

		psbody = new PlayerSpawnerBody(agency.getWorld(), this, Agent.getStartBounds(properties));
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	public Vector2 calcBeginOffsetFromSpawn(Vector2 agentSize) {
		switch(direction) {
			case RIGHT:
				return new Vector2(psbody.getBounds().x + psbody.getBounds().width +
						agentSize.x/2f + SPAWN_SAFETYDIST, psbody.getBounds().y + agentSize.y/2f);
			case UP:
				return new Vector2(psbody.getBounds().x + psbody.getBounds().width/2f,
						psbody.getBounds().y + psbody.getBounds().height + agentSize.y/2f + SPAWN_SAFETYDIST);
			case LEFT:
				return new Vector2(psbody.getBounds().x - agentSize.x/2f - SPAWN_SAFETYDIST,
						psbody.getBounds().y + agentSize.y/2f);
			case DOWN:
			default:
				return new Vector2(psbody.getBounds().x + psbody.getBounds().width/2f,
						psbody.getBounds().y - agentSize.y/2f - SPAWN_SAFETYDIST);
		}
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
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		psbody.dispose();
	}
}
