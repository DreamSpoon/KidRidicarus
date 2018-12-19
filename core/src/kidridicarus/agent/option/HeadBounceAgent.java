package kidridicarus.agent.option;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agent.Agent;

public interface HeadBounceAgent {
	public void onHeadBounce(Agent bouncer, Vector2 fromPos);
}
