package kidridicarus.agency.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.general.AgentSpawnerBody;
import kidridicarus.game.info.KVInfo;

public class AgentSpawner extends Agent {
	private AgentSpawnerBody sbody;
	private boolean isUsed;
	private String sClass;

	public AgentSpawner(Agency agency, AgentDef adef) {
		super(agency, adef);

		sClass = adef.properties.get(KVInfo.Spawn.KEY_SPAWNAGENTCLASS, String.class);

		isUsed = false;

		sbody = new AgentSpawnerBody(this, agency.getWorld(), adef.bounds);
	}

	// update will only be called when the spawner is contacting a spawn trigger
	@Override
	public void update(float delta) {
		if(!isUsed) {
			isUsed = true;
			agency.createAgent(AgentDef.makePointBoundsDef(sClass, sbody.getPosition()));
		}
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
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		sbody.dispose();
	}
}
