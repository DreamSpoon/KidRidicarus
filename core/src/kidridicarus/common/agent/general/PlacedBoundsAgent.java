package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agentproperties.GetPropertyListenerRectangle;
import kidridicarus.common.info.CommonKV;

// an Agent with a placed position and rectangular bounds
public abstract class PlacedBoundsAgent extends PlacedAgent {
	protected abstract Rectangle getBounds();

	protected PlacedBoundsAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		addGetPropertyListener(CommonKV.KEY_BOUNDS, new GetPropertyListenerRectangle() {
			@Override
			public Rectangle getRectangle() { return getBounds(); }
		});
	}
}
