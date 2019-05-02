package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agentproperties.GetPropertyListenerRectangle;
import kidridicarus.common.agentproperties.GetPropertyListenerVector2;
import kidridicarus.common.info.CommonKV;

public abstract class CorpusAgent extends Agent implements Disposable {
	protected AgentBody body;

	protected CorpusAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		addGetPropertyListener(CommonKV.KEY_POSITION, new GetPropertyListenerVector2() {
			@Override
			public Vector2 getVector2() { return getPosition(); }
		});
		addGetPropertyListener(CommonKV.KEY_BOUNDS, new GetPropertyListenerRectangle() {
			@Override
			public Rectangle getRectangle() { return getBounds(); }
		});
		addGetPropertyListener(CommonKV.KEY_VELOCITY, new GetPropertyListenerVector2() {
			@Override
			public Vector2 getVector2() { return getVelocity(); }
		});
	}

	protected Vector2 getPosition() {
		return body != null ? body.getPosition() : null;
	}

	protected Rectangle getBounds() {
		return body != null ? body.getBounds() : null;
	}

	protected Vector2 getVelocity() {
		return body != null ? body.getVelocity() : null;
	}

	// TODO Should body.dispose(); be included here? Implement using interface Disposable or DisposableAgent ?
	@Override
	public void dispose() {
		if(body != null) {
			body.dispose();
			body = null;
		}
	}
}
