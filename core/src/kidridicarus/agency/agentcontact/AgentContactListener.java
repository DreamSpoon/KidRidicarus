package kidridicarus.agency.agentcontact;

import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/*
 * Use a custom filtering (AgentBodyFilter) method to determine contact. When contact occurs, invoke the
 * sensor contact methods. Also use contact.isTouching() for more precise collision detection.
 */
public class AgentContactListener implements ContactListener {
	// I didn't want to remove and add Booleans from the HashMap whenever contact state changed, so here is the solution:
	private class BooleanWrapper {	// TODO find better way to do this
		public boolean isTrue;
		public BooleanWrapper(boolean isTrue) { this.isTrue = isTrue; }
	}

	private HashMap<Integer, BooleanWrapper> allContacts;

	public AgentContactListener() {
		allContacts = new HashMap<Integer, BooleanWrapper>();
	}

	@Override
	public void beginContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		// Use a hash value combo of fixtures A and B to index, because each contact is unique to the
		// combo of fixtures A and B.
		allContacts.put(Objects.hash(contact.getFixtureA(), contact.getFixtureB()), new BooleanWrapper(contact.isTouching()));
		// if actually touching on first contact then do actual begin
		if(contact.isTouching())
			actualBeginContact(contact);
	}

	@Override
	public void endContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		BooleanWrapper bw = allContacts.get(Objects.hash(contact.getFixtureA(), contact.getFixtureB()));
		allContacts.remove(Objects.hash(contact.getFixtureA(), contact.getFixtureB()));
		// if actually touching on last contact then do actual end
		if(bw.isTrue)
			actualEndContact(contact);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	// check for and do actual contact changes
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		BooleanWrapper bw = allContacts.get(Objects.hash(contact.getFixtureA(), contact.getFixtureB()));
		if(!bw.isTrue && contact.isTouching()) {
			bw.isTrue = true;
			actualBeginContact(contact);
		}
		else if(bw.isTrue && !contact.isTouching()) {
			bw.isTrue = false;
			actualEndContact(contact);
		}
	}

	public void actualBeginContact(Contact contact) {
		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		if(objA instanceof AgentContactSensor)
			((AgentContactSensor) objA).onBeginContact((AgentBodyFilter) contact.getFixtureB().getUserData());
		if(objB instanceof AgentContactSensor)
			((AgentContactSensor) objB).onBeginContact((AgentBodyFilter) contact.getFixtureA().getUserData());
	}

	public void actualEndContact(Contact contact) {
		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		if(objA instanceof AgentContactSensor)
			((AgentContactSensor) objA).onEndContact((AgentBodyFilter) contact.getFixtureB().getUserData());
		if(objB instanceof AgentContactSensor)
			((AgentContactSensor) objB).onEndContact((AgentBodyFilter) contact.getFixtureA().getUserData());
	}
}
