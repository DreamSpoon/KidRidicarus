package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.tool.QQ;

/*
 * One-way floor: What goes up must not go down, unless it was already down.
 */
public class SemiSolidFloor extends Agent implements DisposableAgent {
	private Rectangle bounds;
	private SemiSolidFloorBody body;

	public SemiSolidFloor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
QQ.pr("semisolidfloor created, bounds="+Agent.getStartBounds(properties));
		bounds = Agent.getStartBounds(properties);

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
