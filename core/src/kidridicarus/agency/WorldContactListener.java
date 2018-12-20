package kidridicarus.agency;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.bodies.SMB.PipeWarpBody;
import kidridicarus.agent.bodies.SMB.player.MarioBody;
import kidridicarus.agent.bodies.general.AgentSpawnTriggerBody;
import kidridicarus.agent.bodies.general.AgentSpawnerBody;
import kidridicarus.agent.bodies.general.RoomBoxBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.agent.bodies.optional.GroundCheckBody;
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
			case (GameInfo.SPAWNTRIGGER_BIT | GameInfo.SPAWNBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.SPAWNTRIGGER_BIT)
					((AgentSpawnTriggerBody) fixA.getUserData()).onBeginContactSpawnBox((AgentSpawnerBody) fixB.getUserData());
				else
					((AgentSpawnTriggerBody) fixB.getUserData()).onBeginContactSpawnBox((AgentSpawnerBody) fixA.getUserData());
				break;
			// mario's head started touching an interactive tile
			case (GameInfo.GUIDEHEAD_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEHEAD_BIT)
					((MarioBody) fixA.getUserData()).onHeadTileContactStart((AgentBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onHeadTileContactStart((AgentBody) fixA.getUserData());
				break;
			// mario touched a despawn box
			case (GameInfo.GUIDE_BIT | GameInfo.DESPAWN_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_BIT)
					((MarioBody) fixA.getUserData()).onContactDespawn();
				else
					((MarioBody) fixB.getUserData()).onContactDespawn();
				break;
			// mario's foot hit a pipe
			case (GameInfo.GUIDEHEAD_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEHEAD_BIT)
					((MarioBody) fixA.getUserData()).onBeginContactPipe((PipeWarpBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onBeginContactPipe((PipeWarpBody) fixA.getUserData());
				break;
// TODO: fix mario sensor code
/*			// mario's side hit a pipe
			case (GameInfo.MARIOSIDE_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOSIDE_BIT)
					((MarioBody) fixA.getUserData()).onStartTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onStartTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
*/
			// mario's foot hit a pipe
			case (GameInfo.GUIDEFOOT_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEFOOT_BIT)
					((MarioBody) fixA.getUserData()).onBeginContactPipe((PipeWarpBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onBeginContactPipe((PipeWarpBody) fixA.getUserData());
				break;
			// mario's foot hit a horizontal or vertical bound
			case (GameInfo.GUIDEFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEFOOT_BIT)
					((MarioBody) fixA.getUserData()).onFootBeginContactBound((LineSeg) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onFootBeginContactBound((LineSeg) fixA.getUserData());
				break;
			// player agent touched an agent
			case (GameInfo.GUIDE_AGENTSENSOR_BIT | GameInfo.AGENT_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_AGENTSENSOR_BIT)
					((MarioBody) fixA.getUserData()).onContactAgent((AgentBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onContactAgent((AgentBody) fixA.getUserData());
				break;
			// agent touched horizontal or vertical bound
			case (GameInfo.AGENT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.AGENT_BIT)
					((MobileAgentBody) fixA.getUserData()).onContactBoundLine((LineSeg) fixB.getUserData());
				else
					((MobileAgentBody) fixB.getUserData()).onContactBoundLine((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.AGENTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.AGENT_BIT)
					((GroundCheckBody) fixA.getUserData()).onBeginContactGround();
				else
					((GroundCheckBody) fixB.getUserData()).onBeginContactGround();
				break;
			// agent touched another agent
			case (GameInfo.AGENT_BIT):
				((AgentContactBody) fixA.getUserData()).onContactAgent((AgentBody) fixB.getUserData());
				((AgentContactBody) fixB.getUserData()).onContactAgent((AgentBody) fixA.getUserData());
				break;
			// item touched horizontal or vertical bound
			case (GameInfo.ITEM_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ITEM_BIT)
					((MobileAgentBody) fixA.getUserData()).onContactBoundLine((LineSeg) fixB.getUserData());
				else
					((MobileAgentBody) fixB.getUserData()).onContactBoundLine((LineSeg) fixA.getUserData());
				break;
			// an item touched mario
			case (GameInfo.GUIDE_AGENTSENSOR_BIT | GameInfo.ITEM_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDE_AGENTSENSOR_BIT)
					((MarioBody) fixA.getUserData()).onContactItem((AgentBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onContactItem((AgentBody) fixA.getUserData());
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
			case (GameInfo.SPAWNTRIGGER_BIT | GameInfo.SPAWNBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.SPAWNTRIGGER_BIT)
					((AgentSpawnTriggerBody) fixA.getUserData()).onEndContactSpawnBox((AgentSpawnerBody) fixB.getUserData());
				else
					((AgentSpawnTriggerBody) fixB.getUserData()).onEndContactSpawnBox((AgentSpawnerBody) fixA.getUserData());
				break;
			// mario's head stopped touching an interactive tile
			case (GameInfo.GUIDEHEAD_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEHEAD_BIT)
					((MarioBody) fixA.getUserData()).onHeadTileContactEnd((AgentBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onHeadTileContactEnd((AgentBody) fixA.getUserData());
				break;
			// mario's side stopped touching a pipe
			case (GameInfo.GUIDEHEAD_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEHEAD_BIT)
					((MarioBody) fixA.getUserData()).onEndContactPipe((PipeWarpBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onEndContactPipe((PipeWarpBody) fixA.getUserData());
				break;
// TODO: fix mario sensor code
/*			// mario's foot stopped touching a pipe
			case (GameInfo.MARIOSIDE_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOSIDE_BIT)
					((MarioBody) fixA.getUserData()).onEndTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onEndTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
*/
			// mario's foot stopped touching a pipe
			case (GameInfo.GUIDEFOOT_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEFOOT_BIT)
					((MarioBody) fixA.getUserData()).onEndContactPipe((PipeWarpBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onEndContactPipe((PipeWarpBody) fixA.getUserData());
				break;
			case (GameInfo.GUIDEFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke mario's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.GUIDEFOOT_BIT)
					((MarioBody) fixA.getUserData()).onFootEndContactBound((LineSeg) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onFootEndContactBound((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.AGENTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke agent's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.AGENTFOOT_BIT)
					((GroundCheckBody) fixA.getUserData()).onEndContactGround();
				else
					((GroundCheckBody) fixB.getUserData()).onEndContactGround();
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
