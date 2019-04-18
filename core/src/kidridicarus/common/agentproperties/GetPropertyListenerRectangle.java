package kidridicarus.common.agentproperties;

import com.badlogic.gdx.math.Rectangle;

import kidridicarus.agency.agentproperties.GetPropertyListener;

public abstract class GetPropertyListenerRectangle extends GetPropertyListener {
	public abstract Rectangle getRectangle();

	public GetPropertyListenerRectangle() {
		super(Rectangle.class);
	}

	@Override
	public Rectangle get() {
		return getRectangle();
	}
}
