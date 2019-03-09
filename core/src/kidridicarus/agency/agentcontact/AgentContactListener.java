package kidridicarus.agency.agentcontact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/*
 * Use a custom filtering (AgentBodyFilter) method to determine contact. When contact occurs, invoke the
 * sensor contact methods. 
 */
public class AgentContactListener implements ContactListener {
	@Override
	public void beginContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		if(objA instanceof AgentContactSensor)
			((AgentContactSensor) objA).onBeginContact((AgentBodyFilter) contact.getFixtureB().getUserData());
		if(objB instanceof AgentContactSensor)
			((AgentContactSensor) objB).onBeginContact((AgentBodyFilter) contact.getFixtureA().getUserData());
	}

	@Override
	public void endContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		if(objA instanceof AgentContactSensor)
			((AgentContactSensor) objA).onEndContact((AgentBodyFilter) contact.getFixtureB().getUserData());
		if(objB instanceof AgentContactSensor)
			((AgentContactSensor) objB).onEndContact((AgentBodyFilter) contact.getFixtureA().getUserData());
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
