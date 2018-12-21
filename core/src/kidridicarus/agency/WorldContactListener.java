package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import kidridicarus.agencydirector.AgentSensor;
import kidridicarus.agencydirector.GuideSensor;
import kidridicarus.agent.bodies.MobileGroundAgentBody;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.bodies.SMB.PipeWarpBody;
import kidridicarus.agent.bodies.SMB.player.MarioBody;
import kidridicarus.agent.bodies.general.AgentSpawnTriggerBody;
import kidridicarus.agent.bodies.general.AgentSpawnerBody;
import kidridicarus.agent.bodies.general.RoomBoxBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;

public class WorldContactListener implements ContactListener {
	public void beginContact(Contact contact) {
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
				if(fixA instanceof AgentContactBody)
					((AgentContactBody) fixA.getUserData()).onContactAgent((AgentBody) fixB.getUserData());
				if(fixB instanceof AgentContactBody)
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

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}
