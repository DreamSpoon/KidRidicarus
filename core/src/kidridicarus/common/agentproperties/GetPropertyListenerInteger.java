package kidridicarus.common.agentproperties;

import kidridicarus.agency.agentproperties.GetPropertyListener;

public abstract class GetPropertyListenerInteger extends GetPropertyListener {
	public abstract Integer getInteger();

	public GetPropertyListenerInteger() {
		super(Integer.class);
	}

	@Override
	protected Object innerGet() {
		return getInteger();
	}
}
