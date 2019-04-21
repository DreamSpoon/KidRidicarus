package kidridicarus.common.agent.characteragent;

import kidridicarus.common.agent.characteragent.RoleAgentBody.RoleBodyContactFrameOutput;
import kidridicarus.common.agent.characteragent.RoleAgentBody.RoleBodyFrameOutput;

public abstract class RoleAgentCharacter {
	public abstract class RoleCharacterFrameOutput {
		public float timeDelta;
		public RoleCharacterFrameOutput(float timeDelta) { this.timeDelta = timeDelta; }
	}

	public abstract void processContactFrame(RoleBodyContactFrameOutput processContactFrame);
	public abstract RoleCharacterFrameOutput processFrame(RoleBodyFrameOutput bodyFrameOutput);
}
