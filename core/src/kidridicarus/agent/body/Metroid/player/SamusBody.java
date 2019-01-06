package kidridicarus.agent.body.Metroid.player;

import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;
import kidridicarus.agent.Metroid.player.Samus;
import kidridicarus.agent.body.MobileAgentBody;
import kidridicarus.agent.body.sensor.AgentContactSensor;
import kidridicarus.agent.body.sensor.OnGroundSensor;
import kidridicarus.agent.general.Room;
import kidridicarus.info.UInfo;

public class SamusBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(25f);

	private static final float FOOT_WIDTH = UInfo.P2M(10f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private static final float MIN_MOVE_VEL = 0.1f;
	private static final Vector2 STOP_MOVE_IMP = new Vector2(0.15f, 0f);
	// when samus is damaged, the stopping force is halved
	private static final Vector2 STOP_MOVE_DAMAGE_IMP = STOP_MOVE_IMP.cpy().scl(0.5f);
	private static final float MAX_UP_VELOCITY = 2.5f;
	private static final float MAX_DOWN_VELOCITY = 1.8f;

	private static final Vector2 GROUND_MOVE_IMP = new Vector2(0.3f, 0f);
	private static final float MAX_GROUND_MOVE_VEL = 0.9f;
	
	private static final Vector2 AIR_MOVE_IMP = GROUND_MOVE_IMP.scl(1f);
	private static final float MAX_AIR_MOVE_VEL = MAX_GROUND_MOVE_VEL;

	private static final Vector2 JUMP_UP_FORCE = new Vector2(0f, 5.25f);
	private static final Vector2 JUMP_UP_IMP = new Vector2(0f, 2.0f);

	private Samus parent;
	private AgentContactSensor acSensor;
	private OnGroundSensor ogSensor;

	public SamusBody(Samus parent, World world, Vector2 position) {
		super();

		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		createBody(world, position);
		createAgentSensor();
		createGroundSensor();
	}

	/*
	 * TODO: Make samus' body a trapezoid shape (she's a fat bottomed girl, and she makes the rockin' world
	 * go round) so that it will "catch" on to ledges when samus is falling and is moving toward a wall and
	 * there's an opening that's barely large enough to enter. 
	 */
	private void createBody(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0.75f;
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0.001f;	// (default is 0.2f)
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, null, catBits, maskBits, BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		FixtureDef fdef = new FixtureDef();
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT, CFBit.ROOM_BIT, CFBit.ITEM_BIT, CFBit.DESPAWN_BIT);
		acSensor = new AgentContactSensor(this);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, acSensor));
	}

	// create the foot sensor for detecting onGround
	private void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		ogSensor = new OnGroundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, ogSensor));
	}

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	public Room getCurrentRoom() {
		return (Room) acSensor.getFirstContactByClass(Room.class);
	}

	public void applyForce(Vector2 jumpUpForce) {
		b2body.applyForceToCenter(jumpUpForce, true);
	}

	public void doGroundMove(boolean moveRight) {
		if(moveRight && getVelocity().x < MAX_GROUND_MOVE_VEL)
			applyImpulse(GROUND_MOVE_IMP);
		else if(!moveRight && getVelocity().x > -MAX_GROUND_MOVE_VEL)
			applyImpulse(GROUND_MOVE_IMP.cpy().scl(-1f));
	}

	public void doAirMove(boolean moveRight) {
		if(moveRight && getVelocity().x < MAX_AIR_MOVE_VEL)
			applyImpulse(AIR_MOVE_IMP);
		else if(!moveRight && getVelocity().x > -MAX_AIR_MOVE_VEL)
			applyImpulse(AIR_MOVE_IMP.cpy().scl(-1f));
	}

	public void doStopMove(boolean isDamage) {
		// check for zeroing of velocity
		if(getVelocity().x >= -MIN_MOVE_VEL && getVelocity().x <= MIN_MOVE_VEL)
			setVelocity(0f, getVelocity().y);

		Vector2 amount = STOP_MOVE_IMP.cpy();
		if(isDamage)
			amount.scl(STOP_MOVE_DAMAGE_IMP);

		// if moving right then apply impulse left
		if(getVelocity().x > MIN_MOVE_VEL) {
			applyImpulse(amount.scl(-1f));
			if(getVelocity().x < 0f)
				setVelocity(0f, getVelocity().y);
		}
		else if(getVelocity().x < -MIN_MOVE_VEL) {
			applyImpulse(amount);
			if(getVelocity().x > 0f)
				setVelocity(0f, getVelocity().y);
		}
	}

	public void doJumpStart() {
		applyImpulse(JUMP_UP_IMP);
		applyForce(JUMP_UP_FORCE);
	}

	public void doJumpContinue() {
		applyForce(JUMP_UP_FORCE);
	}

	public <T> LinkedList<Agent> getContactsByClass(Class<T> clazz) {
		return acSensor.getContactsByClass(clazz);
	}

	public void clampMove() {
		boolean isRightMove = getVelocity().x > 0f;
		float xvel = Math.abs(getVelocity().x);
		
		if(xvel > MAX_GROUND_MOVE_VEL * 2f)
			xvel *= 0.8f;
		else if(xvel > MAX_GROUND_MOVE_VEL)
			xvel *= 0.9f;

		if(isRightMove)
			setVelocity(xvel, getVelocity().y);
		else
			setVelocity(-xvel, getVelocity().y);
		
		if(getVelocity().y > MAX_UP_VELOCITY)
			setVelocity(getVelocity().x, MAX_UP_VELOCITY);
		else if(getVelocity().y < -MAX_DOWN_VELOCITY)
			setVelocity(getVelocity().x, -MAX_DOWN_VELOCITY);
	}
}
