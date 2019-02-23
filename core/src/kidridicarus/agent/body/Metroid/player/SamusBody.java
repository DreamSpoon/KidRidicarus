package kidridicarus.agent.body.Metroid.player;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;
import kidridicarus.agent.Metroid.player.Samus;
import kidridicarus.agent.body.MobileAgentBody;
import kidridicarus.agent.body.sensor.AgentContactSensor;
import kidridicarus.agent.body.sensor.OnGroundSensor;
import kidridicarus.agent.body.sensor.SolidBoundSensor;
import kidridicarus.agent.general.Room;
import kidridicarus.info.UInfo;
import kidridicarus.tool.B2DFactory;

public class SamusBody extends MobileAgentBody {
	private static final float STAND_BODY_WIDTH = UInfo.P2M(5f);
	private static final float STAND_BODY_HEIGHT = UInfo.P2M(25f);

	private static final float BALL_BODY_WIDTH = UInfo.P2M(8f);
	private static final float BALL_BODY_HEIGHT = UInfo.P2M(10f);

	private static final float FOOT_WIDTH = UInfo.P2M(4f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private static final float MIN_MOVE_VEL = 0.1f;
	private static final Vector2 STOPMOVE_IMP = new Vector2(0.15f, 0f);
	// when samus is damaged, the stopping force is halved (really?)
	private static final Vector2 STOPMOVE_DAMAGE_IMP = STOPMOVE_IMP.cpy().scl(0.5f);
	private static final float MAX_UP_VELOCITY = 1.75f;
	private static final float MAX_DOWN_VELOCITY = 2.5f;

	private static final Vector2 GROUNDMOVE_IMP = new Vector2(0.28f, 0f);
	private static final float MAX_GROUNDMOVE_VEL = 0.85f;

	private static final Vector2 AIRMOVE_IMP = GROUNDMOVE_IMP.scl(0.7f);
	private static final float MAX_AIRMOVE_VEL = MAX_GROUNDMOVE_VEL;

	private static final float JUMPUP_FORCE_DURATION = 0.75f;
	private static final Vector2 JUMPUP_FORCE = new Vector2(0f, 6.75f);
	private static final Vector2 JUMPUP_IMP = new Vector2(0f, 1.25f);

	private World world;
	private Samus parent;
	private AgentContactSensor acSensor;
	private OnGroundSensor ogSensor;
	private boolean isBallForm;
	private Vector2 prevVelocity;
	private SolidBoundSensor sbSensor;

	public SamusBody(Samus parent, World world, Vector2 position) {
		super();
		this.world = world;
		this.parent = parent;
		isBallForm = false;
		defineBody(position);
	}

	private void defineBody(Vector2 position) {
		if(isBallForm)
			setBodySize(BALL_BODY_WIDTH, BALL_BODY_HEIGHT);
		else
			setBodySize(STAND_BODY_WIDTH, STAND_BODY_HEIGHT);

		createBody(position);
		createAgentSensor();
		createGroundSensor();
		prevVelocity = new Vector2(0f, 0f);
	}

	/*
	 * TODO: Make samus' body a trapezoid shape (she's a fat bottomed girl, and she makes the rockin' world
	 * go round) so that it will "catch" on to ledges when samus is falling and is moving toward a wall and
	 * there's an opening that's barely large enough to enter. 
	 */
	private void createBody(Vector2 position) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0.5f;	// floaty
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0.001f;	// (default is 0.2f)
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		sbSensor = new SolidBoundSensor(this);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, sbSensor, catBits, maskBits,
				getBodyWidth(), getBodyHeight());
	}

	public boolean isContactingWall(boolean isRightWall) {
		return sbSensor.isHMoveBlocked(getBounds(), isRightWall);
	}

	private void createAgentSensor() {
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(getBodyWidth()/2f, getBodyHeight()/2f);
		FixtureDef fdef = new FixtureDef();
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT, CFBit.ROOM_BIT, CFBit.ITEM_BIT, CFBit.DESPAWN_BIT);
		acSensor = new AgentContactSensor(this);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, acSensor));
	}

	// create the sensor for detecting onGround
	private void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -getBodyHeight()/2f), 0f);
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

	public void doGroundMove(boolean moveRight) {
		if(moveRight && getVelocity().x < MAX_GROUNDMOVE_VEL)
			applyImpulse(GROUNDMOVE_IMP);
		else if(!moveRight && getVelocity().x > -MAX_GROUNDMOVE_VEL)
			applyImpulse(GROUNDMOVE_IMP.cpy().scl(-1f));
	}

	public void doAirMove(boolean moveRight) {
		if(moveRight && getVelocity().x < MAX_AIRMOVE_VEL)
			applyImpulse(AIRMOVE_IMP);
		else if(!moveRight && getVelocity().x > -MAX_AIRMOVE_VEL)
			applyImpulse(AIRMOVE_IMP.cpy().scl(-1f));
	}

	private float forceTimer;
	public void doJumpStart() {
		applyImpulse(JUMPUP_IMP);
		applyForce(JUMPUP_FORCE);
		forceTimer = 0f;
	}

	public void doJumpContinue(float delta) {
		// timer is updated at start because force was applied in previous frame 
		forceTimer += delta;
		if(forceTimer < JUMPUP_FORCE_DURATION)
			applyForce(JUMPUP_FORCE.cpy().scl(1f - forceTimer / JUMPUP_FORCE_DURATION));
	}

	public void doStopMove(boolean isDamage) {
		// check for zeroing of velocity
		if(getVelocity().x >= -MIN_MOVE_VEL && getVelocity().x <= MIN_MOVE_VEL)
			setVelocity(0f, getVelocity().y);

		Vector2 amount = STOPMOVE_IMP.cpy();
		if(isDamage)
			amount.scl(STOPMOVE_DAMAGE_IMP);

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

	public void clampMove() {
		boolean isRightMove = getVelocity().x > 0f;
		float xvel = Math.abs(getVelocity().x);
		
		if(xvel > MAX_GROUNDMOVE_VEL * 2f)
			xvel *= 0.8f;
		else if(xvel > MAX_GROUNDMOVE_VEL)
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

	public void switchToBallForm() {
		isBallForm = true;
		defineBody(b2body.getPosition());
	}

	public void switchToStandForm() {
		isBallForm = false;
		defineBody(b2body.getPosition());
	}

	public <T> List<Agent> getContactsByClass(Class<T> clazz) {
		return acSensor.getContactsByClass(clazz);
	}

	public void doBounceCheck() {
		// Check for bounce up (no left/right bounces, no down bounces).
		// Since body restitution=0, bounce occurs when current velocity=0 and previous velocity > 0.
		// Check against 0 using velocity epsilon.
		if(UInfo.epsCheck(getVelocity().y, 0f, UInfo.VEL_EPSILON)) {
			float amount = -prevVelocity.y;
			if(amount > MAX_DOWN_VELOCITY-UInfo.VEL_EPSILON)
				amount = MAX_UP_VELOCITY-UInfo.VEL_EPSILON;
			else
				amount = amount * 0.6f;
			setVelocity(getVelocity().x, amount);
		}
	}

	public void postUpdate() {
		prevVelocity.set(getVelocity());
	}
}
