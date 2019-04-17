package kidridicarus.common.agentproperties;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agentproperties.GetPropertyListener;

public abstract class GetPropertyListenerVector2 extends GetPropertyListener {
	public abstract Vector2 getVector2();

	public GetPropertyListenerVector2() {
		super(Vector2.class);
	}

	@Override
	protected Object innerGet() {
		return getVector2();
	}
}
