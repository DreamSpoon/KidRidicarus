package kidridicarus.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.ADefFactory;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.bodies.general.AgentSpawnerBody;
import kidridicarus.info.KVInfo;

public class AgentSpawner extends Agent {
	public enum AgentSpawnClass { NONE, GOOMBA, TURTLE };
	private AgentSpawnerBody sbody;
	private boolean isUsed;
	private boolean isTriggered;
	private AgentSpawnClass agentSpawnClass;

	public AgentSpawner(Agency agency, AgentDef adef) {
		super(agency, adef);

		String rClass = adef.properties.get(KVInfo.KEY_AGENTCLASS, String.class);
		if(rClass.equals(KVInfo.VAL_SPAWNGOOMBA))
			agentSpawnClass = AgentSpawnClass.GOOMBA;
		else if(rClass.equals(KVInfo.VAL_SPAWNTURTLE))
			agentSpawnClass = AgentSpawnClass.TURTLE;

		isUsed = false;
		isTriggered = false;

		sbody = new AgentSpawnerBody(this, agency.getWorld(), adef.bounds);
	}

	@Override
	public void update(float delta) {
		if(isTriggered && !isUsed) {
			isUsed = true;

			switch(agentSpawnClass) {
				case GOOMBA:
					agency.createAgent(ADefFactory.makeGoombaDef(sbody.getPosition()));
					break;
				case TURTLE:
					agency.createAgent(ADefFactory.makeTurtleDef(sbody.getPosition()));
					break;
				default:
					break;
			}
		}
	}

	public void onStartVisibility() {
		isTriggered = true;
	}

	public void onEndVisibility() {
	}

	@Override
	public void draw(Batch batch) {
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
