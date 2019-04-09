package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.SolidAgent;

/*
 * One-way floor: What goes up must not go down, unless it was already down.
 */
public class SemiSolidFloor extends Agent implements SolidAgent, DisposableAgent {
	private Rectangle bounds;
	private SemiSolidFloorBody body;

	public SemiSolidFloor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		bounds = new Rectangle(Agent.getStartBounds(properties));
		// ensure the floor bounds height = zero (essentially, creating a line at top of bounds)
		bounds.y = bounds.y + bounds.height;
		bounds.height = 0f;
		body = new SemiSolidFloorBody(this, agency.getWorld(), bounds);
	}

	@Override
	public Vector2 getPosition() {
		return bounds.getCenter(new Vector2());
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
