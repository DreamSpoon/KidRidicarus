package kidridicarus.common.agent.followbox;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;

public abstract class FollowBox extends CorpusAgent {
	public FollowBox(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	/*
	 * Set the target center position of the follow box, and the box will move on update (mouse joint).
	 */
	public void setTarget(Vector2 position) {
		((FollowBoxBody) body).setPosition(position);
	}
}
