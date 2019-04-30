package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.corpusagent.CorpusAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.tool.AP_Tool;

/*
 * One-way floor: What goes up must not go down, unless it was already down.
 */
public class SemiSolidFloor extends CorpusAgent implements SolidAgent, DisposableAgent {
	public SemiSolidFloor(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		Rectangle bounds = new Rectangle(AP_Tool.getBounds(properties));
		// ensure the floor bounds height = zero (essentially, creating a line at top of bounds)
		bounds.y = bounds.y + bounds.height;
		bounds.height = 0f;
		body = new SemiSolidFloorBody(this, agency.getWorld(), bounds);
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
