package kidridicarus.agency.agentbody;

import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/*
 * Use a custom filtering (AgentBodyFilter) method to determine contact. When contact occurs, invoke the
 * sensor contact methods. Also use contact.isTouching() for more precise contact detection. To implement this,
 * it is necessary to keep a list of all active contacts based on their (fixtureA, fixtureB) pair - note that the
 * pair (fixtureA, fixtureB) is equivalent to the pair (fixtureB, fixtureA).
 * From debugging experience, I've learned that the same Contact object is passed as a parameter for every call
 * to beginContact and endContact. So a workaround was necessary...
 * Treating each pair of fixtures in the contact as a single meta-object (by using the Objects.hash method)
 * allows use of a HashMap to keep a list of current contacts with their isTouching states (since each contact
 * is unique to it's (fixtureA, fixtureB) pair, but the pairs may be given in reverse order).
 * For info on Objects.hash see:
 *   https://stackoverflow.com/questions/11597386/objects-hash-vs-objects-hashcode-clarification-needed
 *
 * A custom preSolver can be used to filter semi-solids (e.g. one-way floors). If the custom pre-solver is null
 * then the default preSolver (default for Agency, anyways) will be used.
 */
public class AgentContactListener implements ContactListener {
	public interface PreSolver { public boolean preSolve(Object otherUserData); }

	private HashMap<Integer, Boolean> allContacts;

	public AgentContactListener() {
		allContacts = new HashMap<Integer, Boolean>();
	}

	@Override
	public void beginContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		// Use a hash value combo of fixtures A and B to index, because each contact is unique to the
		// combo of fixtures A and B.
		allContacts.put(Objects.hash(contact.getFixtureA(), contact.getFixtureB()), contact.isTouching());
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

		int hashAB = Objects.hash(contact.getFixtureA(), contact.getFixtureB());
		Boolean wasTouching = allContacts.get(hashAB);
		allContacts.remove(hashAB);
		// if actually touching on last contact then do actual end
		if(wasTouching)
			actualEndContact(contact);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		boolean isEnabledA = true;
		boolean isEnabledB = true;
		// if fixtureA has a custom preSolver then use it
		if(contact.getFixtureA().getUserData() instanceof AgentBodyFilter &&
				((AgentBodyFilter) contact.getFixtureA().getUserData()).preSolver != null) {
			isEnabledA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).preSolver.
					preSolve(contact.getFixtureB().getUserData());
		}
		// if fixtureB has a custom preSolver then use it
		if(contact.getFixtureB().getUserData() instanceof AgentBodyFilter &&
				((AgentBodyFilter) contact.getFixtureB().getUserData()).preSolver != null) {
			isEnabledB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).preSolver.
					preSolve(contact.getFixtureA().getUserData());
		}
		// apply results of custom presolvers (if no presolvers exist then this will always setEnabled(true) )
		contact.setEnabled(isEnabledA && isEnabledB);
	}

	// check for and do actual contact changes
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		int hashAB = Objects.hash(contact.getFixtureA(), contact.getFixtureB());
		Boolean wasToucing = allContacts.get(hashAB);
		if(!wasToucing && contact.isTouching()) {
			allContacts.put(hashAB, true);
			actualBeginContact(contact);
		}
		else if(wasToucing && !contact.isTouching()) {
			allContacts.put(hashAB, false);
			actualEndContact(contact);
		}
	}

	private void actualBeginContact(Contact contact) {
		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		if(objA instanceof AgentContactSensor)
			((AgentContactSensor) objA).onBeginContact((AgentBodyFilter) contact.getFixtureB().getUserData());
		if(objB instanceof AgentContactSensor)
			((AgentContactSensor) objB).onBeginContact((AgentBodyFilter) contact.getFixtureA().getUserData());
	}

	private void actualEndContact(Contact contact) {
		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		if(objA instanceof AgentContactSensor)
			((AgentContactSensor) objA).onEndContact((AgentBodyFilter) contact.getFixtureB().getUserData());
		if(objB instanceof AgentContactSensor)
			((AgentContactSensor) objB).onEndContact((AgentBodyFilter) contact.getFixtureA().getUserData());
	}
}
