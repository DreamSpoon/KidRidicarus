package kidridicarus.common.agent.agentspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonKV;

public class AgentSpawner extends Agent implements TriggerTakeAgent, DisposableAgent {
	private AgentSpawnerBody sbody;
	private boolean isUsed;
	private String spawnAgentClassAlias;

	public AgentSpawner(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		spawnAgentClassAlias = properties.get(CommonKV.Spawn.KEY_SPAWNAGENTCLASS, "", String.class);
		isUsed = false;
		sbody = new AgentSpawnerBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	// to be called by a spawn trigger contacting this agent
	@Override
	public void onTakeTrigger() {
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
	public void disposeAgent() {
		sbody.dispose();
	}
}
