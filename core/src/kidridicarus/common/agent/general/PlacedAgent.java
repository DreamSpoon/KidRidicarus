package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agentproperties.GetPropertyListenerVector2;
import kidridicarus.common.info.CommonKV;

// an Agent with a placed position
public abstract class PlacedAgent extends Agent {
	protected abstract Vector2 getPosition();

	protected PlacedAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		addGetPropertyListener(CommonKV.KEY_POSITION, new GetPropertyListenerVector2() {
			@Override
			public Vector2 getVector2() { return getPosition(); }
		});
	}
}
