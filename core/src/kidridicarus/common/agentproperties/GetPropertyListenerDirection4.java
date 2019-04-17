package kidridicarus.common.agentproperties;

import kidridicarus.agency.agentproperties.GetPropertyListener;
import kidridicarus.common.tool.Direction4;

public abstract class GetPropertyListenerDirection4 extends GetPropertyListener {
	public abstract Direction4 getDirection4();

	public GetPropertyListenerDirection4() {
		super(Direction4.class);
	}

	@Override
	protected Object innerGet() {
		return getDirection4();
	}
}
