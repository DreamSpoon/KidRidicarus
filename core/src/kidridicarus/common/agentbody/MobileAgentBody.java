package kidridicarus.common.agentbody;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.info.UInfo;

public abstract class MobileAgentBody extends AgentBody {
	protected abstract void defineBody(Rectangle bounds, Vector2 velocity);

	public MobileAgentBody(Agent parent, World world) {
		super(parent, world);
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		defineBody(bounds, new Vector2(0f, 0f));
	}

	public void resetPosition(Vector2 position, boolean keepVelocity) {
		// exit if the new position is the same as current position and velocity can be maintained
		if(position.epsilonEquals(b2body.getPosition(), UInfo.POS_EPSILON) && !keepVelocity)
			return;
		if(keepVelocity) {
			defineBody(new Rectangle(position.x-getBounds().width/2f, position.y-getBounds().height/2f,
					getBounds().width, getBounds().height), b2body.getLinearVelocity());
		}
		else {
			defineBody(new Rectangle(position.x-getBounds().width/2f, position.y-getBounds().height/2f,
					getBounds().width, getBounds().height), new Vector2(0f, 0f));
		}
	}
}
