package kidridicarus.common.agentproperties;

import kidridicarus.agency.agentproperties.GetPropertyListener;

public abstract class GetPropertyListenerInteger extends GetPropertyListener {
	public abstract Integer getInteger();

	public GetPropertyListenerInteger() {
		super(Integer.class);
	}

	@Override
	public Integer get() {
		return getInteger();
	}
}
