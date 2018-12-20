package kidridicarus.agent.optional;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Agent;

public interface HeadBounceAgent {
	public void onHeadBounce(Agent agent, Vector2 fromPos);
}
