package kidridicarus.common.agent.halfactor;

import kidridicarus.common.agentbrain.BrainContactFrameInput;
import kidridicarus.common.agentsprite.SpriteFrameInput;

public abstract class HalfActorBrain {
	public abstract void processContactFrame(BrainContactFrameInput cFrameInput);
	public abstract SpriteFrameInput processFrame(float delta);
}
