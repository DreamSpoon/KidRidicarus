package kidridicarus.game.agent.SMB;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;

public abstract class BasicWalkAgent extends Agent {
	private Vector2 constVelocity = new Vector2();

	public BasicWalkAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
	}

	protected void reverseConstVelocity(boolean x, boolean y) {
		if(x)
			constVelocity.x = -constVelocity.x;
		if(y)
			constVelocity.y = -constVelocity.y;
	}

	protected void setConstVelocity(Vector2 v) {
		setConstVelocity(v.x, v.y);
	}

	protected void setConstVelocity(float x, float y) {
		constVelocity.x = x;
		constVelocity.y = y;
	}

	protected Vector2 getConstVelocity() {
		return constVelocity;
	}
}
