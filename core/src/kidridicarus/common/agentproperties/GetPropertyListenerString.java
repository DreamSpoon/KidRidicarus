package kidridicarus.common.agentproperties;

import kidridicarus.agency.agentproperties.GetPropertyListener;

public abstract class GetPropertyListenerString extends GetPropertyListener {
	public abstract String getString();

	public GetPropertyListenerString() {
		super(String.class);
	}

	@Override
	protected Object innerGet() {
		return getString();
	}
}
