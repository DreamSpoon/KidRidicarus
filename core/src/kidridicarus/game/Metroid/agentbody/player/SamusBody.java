package kidridicarus.game.Metroid.agentbody.player;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentscript.ScriptedBodyState;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.agency.tool.Direction4;
import kidridicarus.common.agent.general.Room;
import kidridicarus.common.agent.general.PipeWarp;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.AgentContactSensor;
import kidridicarus.common.agentbody.sensor.OnGroundSensor;
import kidridicarus.common.agentbody.sensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.game.Metroid.agent.player.Samus;

public class SamusBody extends MobileAgentBody {
	private static final float POSITION_EPS = 0.1f;
	private static final float STAND_BODY_WIDTH = UInfo.P2M(5f);
	private static final float STAND_BODY_HEIGHT = UInfo.P2M(25f);
	private static final float BALL_BODY_WIDTH = UInfo.P2M(8f);
	private static final float BALL_BODY_HEIGHT = UInfo.P2M(10f);
	private static final float FOOT_WIDTH = UInfo.P2M(4f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);
	private static final float GRAVITY_SCALE = 0.5f;	// floaty
	private static final float FRICTION = 0.001f;	// (default is 0.2f)

	private static final CFBitSeq MAINBODY_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAINBODY_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	// agent sensor
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.ROOM_BIT, CommonCF.Alias.ITEM_BIT, CommonCF.Alias.DESPAWN_BIT);
	// agent sensor with contacts disabled (still need room bit)
	private static final CFBitSeq NOCONTACT_AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq NOCONTACT_AS_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT);
	// ground and pipe sensor
	private static final CFBitSeq GROUND_AND_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_AND_PIPE_SENSOR_CFMASK =
			new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT, CommonCF.Alias.PIPE_BIT);

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
	private SolidBoundSensor sbSensor;
	private AgentContactSensor wpSensor;	// warp pipe sensor
	private boolean isBallForm;
	private Vector2 prevVelocity;
	private float forceTimer;
	private boolean isContactEnabled;
	private Fixture mainBodyFixture;
	private Fixture agentSensorFixture;
	private Fixture ogSensorFixture;

	public SamusBody(Samus parent, World world, Vector2 position) {
		super();
		this.world = world;
		this.parent = parent;
		isBallForm = false;
		isContactEnabled = true;
		prevVelocity = new Vector2(0f, 0f);
		defineBody(position);
	}

	private void defineBody(Vector2 position) {
		if(isBallForm)
			setBodySize(BALL_BODY_WIDTH, BALL_BODY_HEIGHT);
		else
			setBodySize(STAND_BODY_WIDTH, STAND_BODY_HEIGHT);

		createBody(position);
		createAgentSensor();
		// the warp pipe sensor is chained to the other sensor, so create it here
		wpSensor = new AgentContactSensor(this);
		createGroundAndPipeSensor();

		// reset previous velocity
		prevVelocity.set(0f, 0f);
	}

	/*
	 * TODO: Make samus' body a trapezoid shape (she's a fat bottomed girl, and she makes the rockin' world
	 * go round) so that it will "catch" on to ledges when samus is falling and is moving toward a wall and
	 * there's an opening that's barely large enough to enter (e.g. the starting point of metroid!).
	 */
	private void createBody(Vector2 position) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = GRAVITY_SCALE;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		sbSensor = new SolidBoundSensor(this);
		CFBitSeq catBits = CommonCF.NO_CONTACT_CFCAT;
		CFBitSeq maskBits = CommonCF.NO_CONTACT_CFMASK;
		if(isContactEnabled) {
			catBits = MAINBODY_CFCAT;
			maskBits = MAINBODY_CFMASK;
		}
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, fdef, sbSensor, catBits, maskBits,
				getBodySize().x, getBodySize().y);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		acSensor = new AgentContactSensor(this);
		CFBitSeq catBits = NOCONTACT_AS_CFCAT;
		CFBitSeq maskBits = NOCONTACT_AS_CFMASK;
		if(isContactEnabled) {
			catBits = AS_CFCAT;
			maskBits = AS_CFMASK;
		}
		agentSensorFixture = B2DFactory.makeBoxFixture(b2body, fdef, acSensor, catBits, maskBits,
				getBodySize().x, getBodySize().y);
	}

	// create the sensor for detecting onGround
	private void createGroundAndPipeSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -getBodySize().y/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		ogSensor = new OnGroundSensor(null);
		// the og sensor chains to the wp sensor, because the wp sensor will be attached to other fixtures
		ogSensor.chainTo(wpSensor);
		CFBitSeq catBits = CommonCF.NO_CONTACT_CFCAT;
		CFBitSeq maskBits = CommonCF.NO_CONTACT_CFMASK;
		if(isContactEnabled) {
			catBits = GROUND_AND_PIPE_SENSOR_CFCAT;
			maskBits = GROUND_AND_PIPE_SENSOR_CFMASK;
		}
		ogSensorFixture = b2body.createFixture(fdef);
		ogSensorFixture.setUserData(new AgentBodyFilter(catBits, maskBits, ogSensor));
	}

	public void switchToBallForm() {
		isBallForm = true;
		defineBody(b2body.getPosition());
	}

	public void switchToStandForm() {
		isBallForm = false;
		defineBody(b2body.getPosition());
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

	public void useScriptedBodyState(ScriptedBodyState sbState) {
		setContactEnabled(sbState.contactEnabled);
		setPosition(sbState.position);
		b2body.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
	}

	private void setContactEnabled(boolean enabled) {
		// exit if no change necessary
		if((isContactEnabled && enabled) || (!isContactEnabled && !enabled))
			return;
		if(enabled) {
			// enable contacts
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = MAINBODY_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = MAINBODY_CFMASK;
			((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = AS_CFCAT;
			((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = AS_CFMASK;
			((AgentBodyFilter) ogSensorFixture.getUserData()).categoryBits = GROUND_AND_PIPE_SENSOR_CFCAT;
			((AgentBodyFilter) ogSensorFixture.getUserData()).maskBits = GROUND_AND_PIPE_SENSOR_CFMASK;
		}
		else {
			// disable contacts
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
			((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = NOCONTACT_AS_CFCAT;
			((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = NOCONTACT_AS_CFMASK;
			((AgentBodyFilter) ogSensorFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) ogSensorFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
		}
		// the contact filters were changed, so let Box2D know to update contacts here
		mainBodyFixture.refilter();
		agentSensorFixture.refilter();
		ogSensorFixture.refilter();
		// update the contacts enabled flag
		isContactEnabled = enabled;
	}

	private void setPosition(Vector2 position) {
		// if the current position is very close to the new position then exit
		if(b2body.getPosition().epsilonEquals(position, POSITION_EPS))
			return;
		defineBody(position);
	}

	/*
	 * Returns warp pipe entrance if pipe sensors are contacting a pipe with entrance direction matching adviceDir.
	 * Returns null otherwise. 
	 */
	public PipeWarp getPipeWarpForAdvice(Direction4 adviceDir) {
		for(Agent pw : wpSensor.getContactsByClass(PipeWarp.class)) {
			if(((PipeWarp) pw).canBodyEnterPipe(getBounds(), adviceDir))
				return (PipeWarp) pw;
		}
		return null;
	}

	public <T> List<T> getContactsByClass(Class<T> clazz) {
		return acSensor.getContactsByClass(clazz);
	}

	public boolean isContactingWall(boolean isRightWall) {
		return sbSensor.isHMoveBlocked(getBounds(), isRightWall);
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
}
