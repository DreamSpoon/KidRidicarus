package kidridicarus.game.SMB.agentbody.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.common.agentbody.sensor.OnGroundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

/*
 * A "control center" for the body, to apply move impulses, etc. in an organized manner.
 * This class has multiple functions:
 *   1) Interpret the World through contacts.
 *   2) Relay information to the body from the World, with filtering/organization.
 *   3) Take blocks of information and translate into body impulses, and apply those body impulses
 *     (e.g. move up, move left, apply jump).
 */
public class LuigiSpine {
	private static final float FOOT_WIDTH = UInfo.P2M(5f);
	private static final float FOOT_HEIGHT = UInfo.P2M(2f);

	// agent sensor (room sensor for now)
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT);
	// ground sensor
	private static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);

	private static final float WALKMOVE_XIMP = 0.025f;
	private static final float DECEL_XIMP = WALKMOVE_XIMP * 1.37f;
	private static final float MIN_WALKVEL = WALKMOVE_XIMP * 2f;
	private static final float MAX_WALKVEL = WALKMOVE_XIMP * 42f;

	private AgentContactHoldSensor acSensor;
	private OnGroundSensor ogSensor;
	private LuigiBody body;
	private Body b2body;

	public LuigiSpine(LuigiBody body, Body b2body) {
		this.body = body;
		this.b2body = b2body;
	}

	public void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		acSensor = new AgentContactHoldSensor(this);
		B2DFactory.makeBoxFixture(b2body, fdef, acSensor, AS_CFCAT, AS_CFMASK,
				body.getBodySize().x, body.getBodySize().y);
	}

	public void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -body.getBodySize().y/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		ogSensor = new OnGroundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(GROUND_SENSOR_CFCAT, GROUND_SENSOR_CFMASK,
				ogSensor));
	}

	public void applySimpleMove(boolean moveRight, float amt) {
		if(moveRight)
			body.applyBodyImpulse(new Vector2(amt, 0f));
		else
			body.applyBodyImpulse(new Vector2(-amt, 0f));
	}

	/*
	 * Apply walk impulse and cap horizontal velocity.
	 */
	public void applyWalkMove(boolean moveRight) {
		if(moveRight && body.getVelocity().x < MAX_WALKVEL) {
			applySimpleMove(moveRight, WALKMOVE_XIMP);
			if(body.getVelocity().x > MAX_WALKVEL)
				body.setVelocity(MAX_WALKVEL, body.getVelocity().y);
		}
		else if(!moveRight && body.getVelocity().x > -MAX_WALKVEL) {
			applySimpleMove(moveRight, WALKMOVE_XIMP);
			if(body.getVelocity().x < -MAX_WALKVEL)
				body.setVelocity(-MAX_WALKVEL, body.getVelocity().y);
		}
	}

	/*
	 * Apply decel impulse and check for min velocity.
	 */
	public void applyDecelMove() {
		if(body.getVelocity().x > MIN_WALKVEL)
			applySimpleMove(true, -DECEL_XIMP);
		else if(body.getVelocity().x < -MIN_WALKVEL)
			applySimpleMove(false, -DECEL_XIMP);
		// if not moving fast enough left/right then zero the horizontal velocity
		else
			body.setVelocity(0f, body.getVelocity().y);
	}

	public Room getCurrentRoom() {
		return (Room) acSensor.getFirstContactByClass(Room.class);
	}

	public boolean isOnGround() {
		return ogSensor.isOnGround();
	}
}
