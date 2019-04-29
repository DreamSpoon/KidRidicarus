package kidridicarus.common.agent.fullactor;

import kidridicarus.agency.agentsprite.SpriteFrameInput;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentbrain.BrainFrameInput;

public abstract class FullActorBrain {
	public abstract void processContactFrame(BrainContactFrameInput cFrameInput);
	public abstract SpriteFrameInput processFrame(BrainFrameInput frameInput);
}
