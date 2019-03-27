package kidridicarus.common.agentbody;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.info.UInfo;

public abstract class MobileAgentBody extends AgentBody {
	protected abstract void defineBody(Vector2 position, Vector2 velocity);

	public MobileAgentBody(Agent parent, World world) {
		super(parent, world);
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		defineBody(bounds.getCenter(new Vector2()), new Vector2(0f, 0f));
	}

	public void setPosition(Vector2 position, boolean keepVelocity) {
		// exit if the new position is the same as current position and velocity can be maintained
		if(position.epsilonEquals(b2body.getPosition(), UInfo.POS_EPSILON) && !keepVelocity)
			return;
		if(keepVelocity)
			defineBody(position, b2body.getLinearVelocity());
		else
			defineBody(position, new Vector2(0f, 0f));
	}
}
