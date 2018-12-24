package kidridicarus.agency.contacts;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.bodies.MobileGroundAgentBody;
import kidridicarus.agent.bodies.SMB.PipeWarpBody;
import kidridicarus.agent.bodies.SMB.player.MarioBody;
import kidridicarus.agent.bodies.general.AgentSpawnTriggerBody;
import kidridicarus.agent.bodies.general.AgentSpawnerBody;
import kidridicarus.agent.bodies.general.DespawnBody;
import kidridicarus.agent.bodies.general.RoomBoxBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.agent.bodies.optional.BumpableTileBody;
import kidridicarus.agent.bodies.sensor.AgentSensor;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.guide.sensor.GuideSensor;

public class WorldContactListener implements ContactListener {
	@Override
	public void beginContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;
		// guide contacted a room
		if(isObjClasses(objA, objB, MarioBody.class, RoomBoxBody.class)) {
			if(objA instanceof MarioBody)
				((MarioBody) objA).onBeginContactRoom((RoomBoxBody) objB);
			else
				((MarioBody) objB).onBeginContactRoom((RoomBoxBody) objA);
		}
		// guide contacted a despawn box
		else if(isObjClasses(objA, objB, MarioBody.class, DespawnBody.class)) {
			if(objA instanceof MarioBody)
				((MarioBody) objA).onContactDespawn();
			else
				((MarioBody) objB).onContactDespawn();
		}
		// guide contacted a pipe
		else if(isObjClasses(objA, objB, GuideSensor.class, PipeWarpBody.class)) {
			if(objA instanceof GuideSensor)
				((GuideSensor) objA).onBeginContact((PipeWarpBody) objB);
			else
				((GuideSensor) objB).onBeginContact((PipeWarpBody) objA);
		}
		// guide contacted a horizontal or vertical bound
		else if(isObjClasses(objA, objB, GuideSensor.class, LineSeg.class)) {
			if(objA instanceof GuideSensor)
				((GuideSensor) objA).onBeginContact((LineSeg) objB);
			else
				((GuideSensor) objB).onBeginContact((LineSeg) objA);
		}
		// guide touched a bumpable tile agent
		else if(isObjClasses(objA, objB, BumpableTileBody.class, GuideSensor.class)) {
			if(objA instanceof BumpableTileBody)
				((MarioBody) ((GuideSensor) objB).getParent()).onBeginContactBumpTile((AgentBody) objA);
			else
				((MarioBody) ((GuideSensor) objA).getParent()).onBeginContactBumpTile((AgentBody) objB);
		}
		// guide touched an agent
		else if(isObjClasses(objA, objB, GuideSensor.class, AgentBody.class)) {
			if(objA instanceof GuideSensor)
				((GuideSensor) objA).onBeginContact((AgentBody) objB);
			else
				((GuideSensor) objB).onBeginContact((AgentBody) objA);
		}
		// spawntrigger contacted a spawner
		else if(isObjClasses(objA, objB, AgentSpawnTriggerBody.class, AgentSpawnerBody.class)) {
			if(objA instanceof AgentSpawnTriggerBody)
				((AgentSpawnTriggerBody) objA).onBeginContactSpawnBox((AgentSpawnerBody) objB);
			else if(objB instanceof AgentSpawnTriggerBody)
				((AgentSpawnTriggerBody) objB).onBeginContactSpawnBox((AgentSpawnerBody) objA);
		}
		// agent/item touched horizontal or vertical bound
		else if(isObjClasses(objA, objB, MobileGroundAgentBody.class, LineSeg.class)) {
			if(objA instanceof MobileGroundAgentBody)
				((MobileGroundAgentBody) objA).onBodyBeginContact((LineSeg) objB);
			else
				((MobileGroundAgentBody) objB).onBodyBeginContact((LineSeg) objA);
		}
		// an agent's sensor contacted a horizontal or vertical bound
		else if(isObjClasses(objA, objB, AgentSensor.class, LineSeg.class)) {
			if(objA instanceof AgentSensor)
				((AgentSensor) objA).onBeginContact((LineSeg) objB);
			else
				((AgentSensor) objB).onBeginContact((LineSeg) objA);
		}
		// an agent contacted another agent
		else if(isObjClasses(objA, objB, AgentBody.class, AgentBody.class)) {
			((AgentContactBody) objA).onContactAgent((AgentBody) objB);
			((AgentContactBody) objB).onContactAgent((AgentBody) objA);
		}
	}

	@Override
	public void endContact(Contact contact) {
		if(!(contact.getFixtureA().getUserData() instanceof AgentBodyFilter))
			return;
		if(!(contact.getFixtureB().getUserData() instanceof AgentBodyFilter))
			return;

		Object objA = ((AgentBodyFilter) contact.getFixtureA().getUserData()).userData;
		Object objB = ((AgentBodyFilter) contact.getFixtureB().getUserData()).userData;

		// mario stopped touching a room
		if(isObjClasses(objA, objB, MarioBody.class, RoomBoxBody.class)) {
			if(objA instanceof MarioBody)
				((MarioBody) objA).onEndContactRoom((RoomBoxBody) objB);
			else
				((MarioBody) objB).onEndContactRoom((RoomBoxBody) objA);
		}
		// guide's head stopped touching a bumpable
		// guide touched a bumpable tile agent
		else if(isObjClasses(objA, objB, BumpableTileBody.class, GuideSensor.class)) {
			if(objA instanceof BumpableTileBody)
				((MarioBody) ((GuideSensor) objB).getParent()).onEndContactBumpTile((AgentBody) objA);
			else
				((MarioBody) ((GuideSensor) objA).getParent()).onEndContactBumpTile((AgentBody) objB);
		}
		// guide stopped touching a pipe
		else if(isObjClasses(objA, objB, GuideSensor.class, PipeWarpBody.class)) {
			if(objA instanceof GuideSensor)
				((GuideSensor) objA).onEndContact((PipeWarpBody) objB);
			else
				((GuideSensor) objB).onEndContact((PipeWarpBody) objA);
		}
		// guide's sensor stopped touching a boundary line
		else if(isObjClasses(objA, objB, GuideSensor.class, LineSeg.class)) {
			if(objA instanceof GuideSensor)
				((GuideSensor) objA).onEndContact((LineSeg) objB);
			else
				((GuideSensor) objB).onEndContact((LineSeg) objA);
		}
		// spawntrigger stopped contacting a spawner
		else if(isObjClasses(objA, objB, AgentSpawnTriggerBody.class, AgentSpawnerBody.class)) {
			if(objA instanceof AgentSpawnTriggerBody)
				((AgentSpawnTriggerBody) objA).onEndContactSpawnBox((AgentSpawnerBody) objB);
			else
				((AgentSpawnTriggerBody) objB).onEndContactSpawnBox((AgentSpawnerBody) objA);
		}
		// invoke agent's foot hit method
		else if(isObjClasses(objA, objB, AgentSensor.class, LineSeg.class)) {
			if(objA instanceof AgentSensor)
				((AgentSensor) objA).onEndContact((LineSeg) objB);
			else
				((AgentSensor) objB).onEndContact((LineSeg) objA);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}

//	private <T, U> boolean testThese2Exact(Object obj1, Object obj2, Class<T> class1, Class<U> class2) {
//		return (obj1.getClass().equals(class1) && obj2.getClass().equals(class2)) ||
//				(obj1.getClass().equals(class2) && obj2.getClass().equals(class1));
//	}
	/*
	 * Source of code:
	 * https://stackoverflow.com/questions/6821810/determine-if-two-java-objects-are-of-the-same-class
	 */
	private <T, U> boolean isObjClasses(Object obj1, Object obj2, Class<T> class1, Class<U> class2) {
		return (class1.isAssignableFrom(obj1.getClass()) && class2.isAssignableFrom(obj2.getClass())) ||
				(class1.isAssignableFrom(obj2.getClass()) && class2.isAssignableFrom(obj1.getClass()));
	}

/*	public void beginContact(Contact contact) {
		Fixture fixA, fixB;
		int cdef;
		fixA = contact.getFixtureA();
		fixB = contact.getFixtureB();
		cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		switch(cdef) {
			// mario touched a room
			case (GameInfo.GUIDE_BIT | GameInfo.ROOMBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_BIT)
					((MarioBody) fixA.getUserData()).onBeginContactRoom((RoomBoxBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onBeginContactRoom((RoomBoxBody) fixA.getUserData());
				break;
			// mario touched a despawn box
			case (GameInfo.GUIDE_BIT | GameInfo.DESPAWN_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_BIT)
					((MarioBody) fixA.getUserData()).onContactDespawn();
				else
					((MarioBody) fixB.getUserData()).onContactDespawn();
				break;
			// mario's foot hit a pipe
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onBeginContact((PipeWarpBody) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onBeginContact((PipeWarpBody) fixA.getUserData());
				break;
			// mario's foot hit a horizontal or vertical bound
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onBeginContact((LineSeg) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onBeginContact((LineSeg) fixA.getUserData());
				break;
			// guide touched an agent
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.AGENT_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onBeginContact((AgentBody) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onBeginContact((AgentBody) fixA.getUserData());
				break;
			// an item touched mario
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.ITEM_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onBeginContact((AgentBody) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onBeginContact((AgentBody) fixA.getUserData());
				break;
			// mario's head started touching an interactive tile
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onBeginContact((AgentBody) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onBeginContact((AgentBody) fixA.getUserData());
				break;
			case (GameInfo.SPAWNTRIGGER_BIT | GameInfo.SPAWNBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.SPAWNTRIGGER_BIT)
					((AgentSpawnTriggerBody) fixA.getUserData()).onBeginContactSpawnBox((AgentSpawnerBody) fixB.getUserData());
				else
					((AgentSpawnTriggerBody) fixB.getUserData()).onBeginContactSpawnBox((AgentSpawnerBody) fixA.getUserData());
				break;
			// agent touched horizontal or vertical bound
			case (GameInfo.AGENT_BIT | GameInfo.BOUNDARY_BIT):
			case (GameInfo.ITEM_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.AGENT_BIT &&
					fixA.getUserData() instanceof MobileGroundAgentBody)
					((MobileGroundAgentBody) fixA.getUserData()).onBodyBeginContact((LineSeg) fixB.getUserData());
				else if(fixB.getUserData() instanceof MobileGroundAgentBody)
					((MobileGroundAgentBody) fixB.getUserData()).onBodyBeginContact((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.AGENT_SENSOR_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.AGENT_SENSOR_BIT)
					((AgentSensor) fixA.getUserData()).onBeginContact((LineSeg) fixB.getUserData());
				else
					((AgentSensor) fixB.getUserData()).onBeginContact((LineSeg) fixA.getUserData());
				break;
			// agent touched another agent
			case (GameInfo.AGENT_BIT):
				if(fixA.getUserData() instanceof AgentContactBody)
					((AgentContactBody) fixA.getUserData()).onContactAgent((AgentBody) fixB.getUserData());
				if(fixB.getUserData() instanceof AgentContactBody)
					((AgentContactBody) fixB.getUserData()).onContactAgent((AgentBody) fixA.getUserData());
				break;
			default:
				break;
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixA, fixB;
		int cdef;

		fixA = contact.getFixtureA();
		fixB = contact.getFixtureB();
		cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		switch(cdef) {
			// mario stopped touching a room
			case (GameInfo.GUIDE_BIT | GameInfo.ROOMBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_BIT)
					((MarioBody) fixA.getUserData()).onEndContactRoom((RoomBoxBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onEndContactRoom((RoomBoxBody) fixA.getUserData());
				break;
			// mario's head stopped touching an interactive tile
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onEndContact((AgentBody) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onEndContact((AgentBody) fixA.getUserData());
				break;
			// mario's side stopped touching a pipe
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onEndContact((PipeWarpBody) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onEndContact((PipeWarpBody) fixA.getUserData());
				break;
			case (GameInfo.GUIDE_SENSOR_BIT | GameInfo.BOUNDARY_BIT):
				// invoke mario's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_SENSOR_BIT)
					((GuideSensor) fixA.getUserData()).onEndContact((LineSeg) fixB.getUserData());
				else
					((GuideSensor) fixB.getUserData()).onEndContact((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.SPAWNTRIGGER_BIT | GameInfo.SPAWNBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.SPAWNTRIGGER_BIT)
					((AgentSpawnTriggerBody) fixA.getUserData()).onEndContactSpawnBox((AgentSpawnerBody) fixB.getUserData());
				else
					((AgentSpawnTriggerBody) fixB.getUserData()).onEndContactSpawnBox((AgentSpawnerBody) fixA.getUserData());
				break;
			case (GameInfo.AGENT_SENSOR_BIT | GameInfo.BOUNDARY_BIT):
				// invoke agent's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.AGENT_SENSOR_BIT)
					((AgentSensor) fixA.getUserData()).onEndContact((LineSeg) fixB.getUserData());
				else
					((AgentSensor) fixB.getUserData()).onEndContact((LineSeg) fixA.getUserData());
				break;
			default:
				break;
		}
	}
*/
}
