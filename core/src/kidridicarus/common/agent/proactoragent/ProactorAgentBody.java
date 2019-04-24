package kidridicarus.common.agent.proactoragent;

import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbody.MotileAgentBody;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;

public abstract class ProactorAgentBody extends MotileAgentBody {
	public abstract BrainContactFrameInput processContactFrame();
	public abstract BrainFrameInput processFrame(float delta);

	public ProactorAgentBody(Agent parent, World world) {
		super(parent, world);
	}
}
