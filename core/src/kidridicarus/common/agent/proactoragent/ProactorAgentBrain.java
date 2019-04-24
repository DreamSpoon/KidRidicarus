package kidridicarus.common.agent.proactoragent;

import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;

public abstract class ProactorAgentBrain {
	public abstract void processContactFrame(BrainContactFrameInput cFrameInput);
	public abstract SpriteFrameInput processFrame(BrainFrameInput frameInput);
}
