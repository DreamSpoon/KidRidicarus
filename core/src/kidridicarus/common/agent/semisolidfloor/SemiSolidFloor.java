package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.tool.AP_Tool;

/*
 * One-way floor: What goes up must not go down, unless it was already down.
 */
public class SemiSolidFloor extends PlacedBoundsAgent implements SolidAgent, DisposableAgent {
	private Rectangle bounds;
	private SemiSolidFloorBody body;

	public SemiSolidFloor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		bounds = new Rectangle(AP_Tool.getBounds(properties));
		// ensure the floor bounds height = zero (essentially, creating a line at top of bounds)
		bounds.y = bounds.y + bounds.height;
		bounds.height = 0f;
		body = new SemiSolidFloorBody(this, agency.getWorld(), bounds);
	}

	@Override
	protected Vector2 getPosition() {
		return bounds.getCenter(new Vector2());
	}

	@Override
	protected Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
