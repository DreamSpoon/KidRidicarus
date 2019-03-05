package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.info.AgencyKV;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agentbody.general.AgentSpawnerBody;

public class AgentSpawner extends Agent implements UpdatableAgent {
	private AgentSpawnerBody sbody;
	private boolean isUsed;
	private String spawnAgentClassAlias;

	public AgentSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		spawnAgentClassAlias = properties.get(AgencyKV.Spawn.KEY_SPAWNAGENTCLASS, "", String.class);
		isUsed = false;
		sbody = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	// update will only be called when the spawner is contacting a spawn trigger
	@Override
	public void update(float delta) {
		if(!isUsed) {
			isUsed = true;
			agency.createAgent(Agent.createPointAP(spawnAgentClassAlias, sbody.getPosition()));
		}
	}

	@Override
	public Vector2 getPosition() {
		return sbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return sbody.getBounds();
	}

	@Override
	public void dispose() {
		sbody.dispose();
	}
}
