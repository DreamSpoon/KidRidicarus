package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBody;

public abstract class PlayerAgentBody extends AgentBody {
	private Vector2 prevPosition;
	private Vector2 prevVelocity;

	public PlayerAgentBody(Agent parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		prevPosition = position.cpy();
		prevVelocity = velocity.cpy();
	}

	public void resetPrevValues() {
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
