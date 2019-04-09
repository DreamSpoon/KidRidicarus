package kidridicarus.agency.agentcontact;

import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.info.CommonCF;

/*
 * Use a custom filtering (AgentBodyFilter) method to determine contact. When contact occurs, invoke the
 * sensor contact methods. Also use contact.isTouching() for more precise contact detection.
 * From debugging experience, I've learned that the same Contact object is used for every call to
 * beginContact and endContact. So a workaround was necessary...
 * Treating each pair of fixtures in the contact as a single meta-object (by using the Objects.hash method)
 * allows use of a HashMap to keep a list of current contacts with their isTouching states (since each contact
 * is unique to it's { fixtureA, fixtureB } pair).
 * For info on Objects.hash see:
 *   https://stackoverflow.com/questions/11597386/objects-hash-vs-objects-hashcode-clarification-needed
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
		// if fixture A and fixture B have AgentBodyFilter refs then do one-way-floor check
		if(contact.getFixtureA().getUserData() instanceof AgentBodyFilter &&
				contact.getFixtureB().getUserData() instanceof AgentBodyFilter) {
			contact.setEnabled(isFixtureContact((AgentBodyFilter) contact.getFixtureA().getUserData(),
					(AgentBodyFilter) contact.getFixtureB().getUserData()));
		}
	}

	private static boolean isFixtureContact(AgentBodyFilter filterA, AgentBodyFilter filterB) {
		/*
		 * If neither fixture has the SEMISOLID_BIT then do the test like normal.
		 * If both fixtures have the SEMISOLID_BIT then ignore the bit completely - they cannot collide
		 * because the one will always be below the other! (or visa versa, so always true)
		 * If A has the semi-solid category bit set and B has the semi-solid mask bit set,
		 * and if B is above A, then return true because B struck the the semi-solid floor A from above.
		 * If A has the semi-solid category bit set and B has the semi-solid mask bit set,
		 * and if B is below A, then B cannot strike the semi-solid floor A from below - so remove the
		 * SEMISOLID_BIT and do the test as if the SEMISOLID_BIT was not present.
		 */
		CFBitSeq catBitsA = filterA.categoryBits;
		CFBitSeq catBitsB = filterB.categoryBits;

		// if both fixtures have the SEMISOLID_BIT then ignore the bit completely - they cannot collide
		if(catBitsA.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				catBitsB.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
			// remove the semi-solid bit from catBitsA and catBitsB
			catBitsA = catBitsA.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
			catBitsB = catBitsB.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
		}
		// if fixture A is semi solid and fixture B can contact semi-solid...
		else {
			Agent agentA = AgentBodyFilter.getAgentFromFilter(filterA);
			Agent agentB = AgentBodyFilter.getAgentFromFilter(filterB);

			// if fixture A is the semi-solid and fixture B maybe contacts it...
			if(catBitsA.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				filterB.maskBits.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
				// If fixture B is above fixture A then do contact test like normal,
				// otherwise remove the semi-solid bit from fixture A.
				// If the top of the semi-solid fixture is above the bottom of the other fixture then remove
				// semi-solid bit and do contact test.
				if(agentA != null && agentB != null &&
						agentA.getBounds().y + agentA.getBounds().height > agentB.getBounds().y) {
					catBitsA = catBitsA.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
				}
			}
			// if fixture B is the semi-solid and fixture A maybe contacts it...
			else if(catBitsB.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero() &&
				filterA.maskBits.and(CommonCF.Alias.SEMISOLID_FLOOR_BIT).isNonZero()) {
				// If fixture A is above fixture B then do contact test like normal,
				// otherwise remove the semi-solid bit from fixture B.
				// If the top of the semi-solid fixture is above the bottom of the other fixture then
				// remove semi-solid bit and do contact test.
				if(agentA != null && agentB != null &&
						agentB.getBounds().y + agentB.getBounds().height > agentA.getBounds().y) {
					catBitsB = catBitsB.and(new CFBitSeq(true, CommonCF.Alias.SEMISOLID_FLOOR_BIT));
				}
			}
		}

		return catBitsA.and(filterB.maskBits).isNonZero() && catBitsB.and(filterA.maskBits).isNonZero();
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
