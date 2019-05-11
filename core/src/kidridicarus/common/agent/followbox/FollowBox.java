package kidridicarus.common.agent.followbox;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;

public abstract class FollowBox extends CorpusAgent {
	public FollowBox(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
	}

	/*
	 * Set the target center position of the follow box, and the box will move on update (mouse joint).
	 */
	public void setTarget(Vector2 position) {
		((FollowBoxBody) body).setPosition(position);
	}
}
