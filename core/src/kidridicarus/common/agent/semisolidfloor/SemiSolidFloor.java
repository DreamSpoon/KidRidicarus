package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.SolidAgent;
import kidridicarus.common.tool.AP_Tool;

/*
 * One-way floor: What goes up must not go down, unless it was already down.
 */
public class SemiSolidFloor extends CorpusAgent implements SolidAgent {
	public SemiSolidFloor(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		Rectangle bounds = new Rectangle(AP_Tool.getBounds(properties));
		// ensure the floor bounds height = zero (essentially, creating a line at top of bounds)
		bounds.y = bounds.y + bounds.height;
		bounds.height = 0f;
		body = new SemiSolidFloorBody(this, agentHooks.getWorld(), bounds);
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}
}
