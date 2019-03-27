package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbody.MobileAgentBody;

public abstract class PlayerAgentBody extends MobileAgentBody {
	private Vector2 prevPosition;
	private Vector2 prevVelocity;

	public PlayerAgentBody(Agent parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		prevPosition = position.cpy();
		prevVelocity = velocity.cpy();
	}

	protected void resetPrevValues(Vector2 position, Vector2 velocity) {
		prevPosition.set(position);
		prevVelocity.set(velocity);
	}

	public void postUpdate() {
		prevPosition.set(b2body.getPosition());
		prevVelocity.set(b2body.getLinearVelocity());
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}
}
