package kidridicarus.game.agentbody;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.AgentBody;

public abstract class PlayerAgentBody extends AgentBody {
	private Vector2 prevPosition;
	private Vector2 prevVelocity;

	public PlayerAgentBody(Vector2 position, Vector2 velocity) {
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
