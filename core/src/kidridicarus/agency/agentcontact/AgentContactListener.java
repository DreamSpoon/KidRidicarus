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
