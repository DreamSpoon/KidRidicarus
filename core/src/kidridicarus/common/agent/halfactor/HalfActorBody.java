package kidridicarus.common.agent.halfactor;

import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentbrain.BrainContactFrameInput;

public abstract class HalfActorBody extends AgentBody {
	public abstract BrainContactFrameInput processContactFrame();

	public HalfActorBody(Agent parent, World world) {
		super(parent, world);
	}
}
