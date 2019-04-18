package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agentproperties.GetPropertyListenerVector2;
import kidridicarus.common.info.CommonKV;

//an Agent with a placed position, rectangular bounds, and velocity
public abstract class MotileBoundsAgent extends PlacedBoundsAgent {
	protected abstract Vector2 getVelocity();

	protected MotileBoundsAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		addGetPropertyListener(CommonKV.KEY_VELOCITY, new GetPropertyListenerVector2() {
			@Override
			public Vector2 getVector2() { return getVelocity(); }
		});
	}
}
