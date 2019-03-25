package kidridicarus.common.agent.despawnbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;

public class DespawnBox extends Agent implements DisposableAgent {
	private DespawnBoxBody body;

	public DespawnBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new DespawnBoxBody(this, agency.getWorld(), Agent.getStartBounds(properties));
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}