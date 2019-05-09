package kidridicarus.common.agent.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;

public abstract class CorpusAgent extends Agent implements Disposable {
	protected AgentBody body;

	protected CorpusAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = null;
		agency.addAgentPropertyListener(this, CommonKV.KEY_POSITION, new AgentPropertyListener<Vector2>(Vector2.class) {
			@Override
			public Vector2 getValue() { return getPosition(); }
		});
		agency.addAgentPropertyListener(this, CommonKV.KEY_BOUNDS, new AgentPropertyListener<Rectangle>(Rectangle.class) {
			@Override
			public Rectangle getValue() { return getBounds(); }
		});
		agency.addAgentPropertyListener(this, CommonKV.KEY_VELOCITY, new AgentPropertyListener<Vector2>(Vector2.class) {
			@Override
			public Vector2 getValue() { return getVelocity(); }
		});
	}

	// The following 3 methods (get position, bounds, velocity) are not coded inline above so that subclasses can
	// override them as needed.

	protected Vector2 getPosition() {
		return body != null ? body.getPosition() : null;
	}

	protected Rectangle getBounds() {
		return body != null ? body.getBounds() : null;
	}

	protected Vector2 getVelocity() {
		return body != null ? body.getVelocity() : null;
	}

	@Override
	public void dispose() {
		if(body != null) {
			body.dispose();
			body = null;
		}
	}
}
