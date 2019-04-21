package kidridicarus.common.agent.characteragent;

import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbody.MotileAgentBody;

public abstract class RoleAgentBody extends MotileAgentBody {
	public abstract class RoleBodyContactFrameOutput {}
	public abstract class RoleBodyFrameOutput {
		public float timeDelta;
		public RoleBodyFrameOutput(float timeDelta) { this.timeDelta = timeDelta; }
	}

	protected abstract RoleBodyContactFrameOutput processContactFrame();
	protected abstract RoleBodyFrameOutput processFrame(float delta);

	public RoleAgentBody(Agent parent, World world) {
		super(parent, world);
	}
}
