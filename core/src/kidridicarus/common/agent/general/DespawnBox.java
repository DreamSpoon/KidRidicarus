package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agentbody.general.DespawnBody;

public class DespawnBox extends Agent {
	private DespawnBody dsBody;

	public DespawnBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		dsBody = new DespawnBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	@Override
	public Vector2 getPosition() {
		return dsBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return dsBody.getBounds();
	}

	@Override
	public void dispose() {
		dsBody.dispose();
	}
}