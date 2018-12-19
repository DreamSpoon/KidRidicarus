package kidridicarus.agent.general;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.bodies.general.DespawnBody;

public class DespawnBox extends Agent {
	private DespawnBody dsbody;

	public DespawnBox(Agency agency, AgentDef adef) {
		super(agency, adef);
		dsbody = new DespawnBody(this, agency.getWorld(), adef.bounds);
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void draw(Batch batch) {
	}

	@Override
	public Vector2 getPosition() {
		return dsbody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return dsbody.getBounds();
	}

	@Override
	public void dispose() {
		dsbody.dispose();
	}
}