package kidridicarus.worldrunner;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import kidridicarus.bodies.BotGroundCheckBody;
import kidridicarus.bodies.BotTouchBotBody;
import kidridicarus.bodies.PlayerBody;
import kidridicarus.bodies.RobotBody;
import kidridicarus.bodies.MobileRobotBody;
import kidridicarus.bodies.SMB.MarioBody;
import kidridicarus.bodies.SMB.PipeWarpBody;
import kidridicarus.bodies.general.RobotSpawnBoxBody;
import kidridicarus.bodies.general.RobotSpawnTriggerBody;
import kidridicarus.bodies.general.RoomBoxBody;
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
			case (GameInfo.MARIO_BIT | GameInfo.ROOMBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_BIT)
					((MarioBody) fixA.getUserData()).onBeginContactRoom((RoomBoxBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onBeginContactRoom((RoomBoxBody) fixA.getUserData());
				break;
			case (GameInfo.SPAWNTRIGGER_BIT | GameInfo.SPAWNBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.SPAWNTRIGGER_BIT)
					((RobotSpawnTriggerBody) fixA.getUserData()).onBeginContactSpawnBox((RobotSpawnBoxBody) fixB.getUserData());
				else
					((RobotSpawnTriggerBody) fixB.getUserData()).onBeginContactSpawnBox((RobotSpawnBoxBody) fixA.getUserData());
				break;
			// mario's head started touching an interactive tile
			case (GameInfo.MARIOHEAD_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onHeadTileContactStart((RobotBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onHeadTileContactStart((RobotBody) fixA.getUserData());
				break;
			// mario touched a despawn box
			case (GameInfo.MARIO_BIT | GameInfo.DESPAWN_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_BIT)
					((PlayerBody) fixA.getUserData()).onTouchDespawn();
				else
					((PlayerBody) fixB.getUserData()).onTouchDespawn();
				break;
			// mario's foot hit a pipe
			case (GameInfo.MARIOHEAD_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onStartTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onStartTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
// TODO: fix mario sensor code
/*			// mario's side hit a pipe
			case (GameInfo.MARIOSIDE_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOSIDE_BIT)
					((PlayerBody) fixA.getUserData()).onStartTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onStartTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
*/
			// mario's foot hit a pipe
			case (GameInfo.MARIOFOOT_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onStartTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onStartTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
			// mario's foot hit a horizontal or vertical bound
			case (GameInfo.MARIOFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onFootTouchBound((LineSeg) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onFootTouchBound((LineSeg) fixA.getUserData());
				break;
			// mario touched a robot
			case (GameInfo.MARIO_ROBOSENSOR_BIT | GameInfo.ROBOT_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_ROBOSENSOR_BIT)
					((PlayerBody) fixA.getUserData()).onTouchRobot((RobotBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onTouchRobot((RobotBody) fixA.getUserData());
				break;
			// robot touched horizontal or vertical bound
			case (GameInfo.ROBOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOT_BIT)
					((MobileRobotBody) fixA.getUserData()).onTouchBoundLine((LineSeg) fixB.getUserData());
				else
					((MobileRobotBody) fixB.getUserData()).onTouchBoundLine((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.ROBOTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOT_BIT)
					((BotGroundCheckBody) fixA.getUserData()).onTouchGround();
				else
					((BotGroundCheckBody) fixB.getUserData()).onTouchGround();
				break;
			// robot touched another robot
			case (GameInfo.ROBOT_BIT):
				((BotTouchBotBody) fixA.getUserData()).onTouchRobot((RobotBody) fixB.getUserData());
				((BotTouchBotBody) fixB.getUserData()).onTouchRobot((RobotBody) fixA.getUserData());
				break;
			// item touched horizontal or vertical bound
			case (GameInfo.ITEM_BIT | GameInfo.BOUNDARY_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.ITEM_BIT)
					((MobileRobotBody) fixA.getUserData()).onTouchBoundLine((LineSeg) fixB.getUserData());
				else
					((MobileRobotBody) fixB.getUserData()).onTouchBoundLine((LineSeg) fixA.getUserData());
				break;
			// an item touched mario
			case (GameInfo.MARIO_ROBOSENSOR_BIT | GameInfo.ITEM_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_ROBOSENSOR_BIT)
					((PlayerBody) fixA.getUserData()).onTouchItem((RobotBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onTouchItem((RobotBody) fixA.getUserData());
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
			case (GameInfo.MARIO_BIT | GameInfo.ROOMBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIO_BIT)
					((MarioBody) fixA.getUserData()).onEndContactRoom((RoomBoxBody) fixB.getUserData());
				else
					((MarioBody) fixB.getUserData()).onEndContactRoom((RoomBoxBody) fixA.getUserData());
				break;
			case (GameInfo.SPAWNTRIGGER_BIT | GameInfo.SPAWNBOX_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.SPAWNTRIGGER_BIT)
					((RobotSpawnTriggerBody) fixA.getUserData()).onEndContactSpawnBox((RobotSpawnBoxBody) fixB.getUserData());
				else
					((RobotSpawnTriggerBody) fixB.getUserData()).onEndContactSpawnBox((RobotSpawnBoxBody) fixA.getUserData());
				break;
			// mario's head stopped touching an interactive tile
			case (GameInfo.MARIOHEAD_BIT | GameInfo.BANGABLE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onHeadTileContactEnd((RobotBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onHeadTileContactEnd((RobotBody) fixA.getUserData());
				break;
			// mario's side stopped touching a pipe
			case (GameInfo.MARIOHEAD_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOHEAD_BIT)
					((PlayerBody) fixA.getUserData()).onEndTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onEndTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
// TODO: fix mario sensor code
/*			// mario's foot stopped touching a pipe
			case (GameInfo.MARIOSIDE_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOSIDE_BIT)
					((PlayerBody) fixA.getUserData()).onEndTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onEndTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
*/
			// mario's foot stopped touching a pipe
			case (GameInfo.MARIOFOOT_BIT | GameInfo.PIPE_BIT):
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onEndTouchPipe((PipeWarpBody) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onEndTouchPipe((PipeWarpBody) fixA.getUserData());
				break;
			case (GameInfo.MARIOFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke mario's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.MARIOFOOT_BIT)
					((PlayerBody) fixA.getUserData()).onFootLeaveBound((LineSeg) fixB.getUserData());
				else
					((PlayerBody) fixB.getUserData()).onFootLeaveBound((LineSeg) fixA.getUserData());
				break;
			case (GameInfo.ROBOTFOOT_BIT | GameInfo.BOUNDARY_BIT):
				// invoke robot 's foot hit method
				if(fixA.getFilterData().categoryBits == GameInfo.ROBOTFOOT_BIT)
					((BotGroundCheckBody) fixA.getUserData()).onLeaveGround();
				else
					((BotGroundCheckBody) fixB.getUserData()).onLeaveGround();
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
