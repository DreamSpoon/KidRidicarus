package kidridicarus.game.agent.SMB.player.mario;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.agency.agentscript.ScriptedBodyState;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.OnGroundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.Direction4;
import kidridicarus.game.agent.SMB.TileBumpTakeAgent;
import kidridicarus.game.agent.SMB.other.pipewarp.PipeWarp;

public class MarioBody extends MobileAgentBody {
	private static final float POSITION_EPS = 0.1f;
	private static final float GRAVITY_SCALE = 1f;
	public static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	public static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));

	private static final CFBitSeq MAINBODY_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAINBODY_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq GROUND_AND_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_AND_PIPE_SENSOR_CFMASK =
			new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT, CommonCF.Alias.PIPEWARP_BIT);

	private static final CFBitSeq SIDE_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq SIDE_PIPE_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.PIPEWARP_BIT);

	private static final CFBitSeq BUMPTILE_AND_PIPE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq BUMPTILE_AND_PIPE_SENSOR_CFMASK =
			new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT, CommonCF.Alias.PIPEWARP_BIT);

	// agent sensor
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.POWERUP_BIT, CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.COLLISIONMAP_BIT);
	// agent sensor with contacts disabled (still needs room bit)
	private static final CFBitSeq NOCONTACT_AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq NOCONTACT_AS_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.COLLISIONMAP_BIT);

	private static final float MARIO_WALKMOVE_XIMP = 0.025f;
	public static final float MARIO_MIN_WALKSPEED = MARIO_WALKMOVE_XIMP * 2;
	private static final float MARIO_RUNMOVE_XIMP = MARIO_WALKMOVE_XIMP * 1.5f;
	private static final float DECEL_XIMP = MARIO_WALKMOVE_XIMP * 1.37f;
	private static final float MARIO_BRAKE_XIMP = MARIO_WALKMOVE_XIMP * 2.75f;
	public static final float MARIO_BRAKE_TIME = 0.2f;
	private static final float MARIO_MAX_WALKVEL = MARIO_WALKMOVE_XIMP * 42f;
	public static final float MARIO_MAX_RUNVEL = MARIO_MAX_WALKVEL * 1.65f;
	private static final float MARIO_MAX_DUCKSLIDE_VEL = MARIO_MAX_WALKVEL * 0.65f;
	private static final float MARIO_DUCKSLIDE_XIMP = MARIO_WALKMOVE_XIMP * 1f;
	private static final float MARIO_AIRMOVE_XIMP = 0.04f;

	private Mario parent;
	private Agency agency;

	private OnGroundSensor ogSensor;
	private AgentContactHoldSensor wpSensor;
	private AgentContactHoldSensor btSensor;
	private AgentContactHoldSensor acSensor;
	private AgentContactBeginSensor acBeginSensor;
	private Fixture agentSensorFixture;

	private boolean isContactEnabled;
	private Fixture ogSensorFixture;
	private Fixture rightSideSensorFixture;
	private Fixture leftSideSensorFixture;
	private Fixture topSensorFixture;
	private Fixture mainBodyFixture;

	private Vector2 prevPosition;
	private Vector2 prevVelocity;

	public MarioBody(Mario parent, Agency agency, Vector2 position, boolean isBig, boolean isDucking) {
		this.parent = parent;
		this.agency = agency;

		isContactEnabled = true;
		defineBody(position, new Vector2(0f, 0f), isBig, isDucking);
	}

	public void defineBody(Vector2 position, Vector2 velocity, boolean isBig, boolean isDucking) {
		createBody(position, velocity, isBig, isDucking);

		// the warp pipe sensor is chained to other sensors, so create it here
		wpSensor = new AgentContactHoldSensor(this);
		createGroundAndPipeSensor(isBig, isDucking);
		createSidePipeSensors();
		createBumpTileAndPipeSensor(isBig, isDucking);

		// warp pipe sensor is not chained to this sensor
		createAgentSensor();
	}

	private void createBody(Vector2 position, Vector2 velocity, boolean isBig, boolean isDucking) {
		if(b2body != null)
			agency.getWorld().destroyBody(b2body);

		if(isBig && !isDucking)
			setBodySize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBodySize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);

		prevPosition = position.cpy();
		prevVelocity = new Vector2(0f, 0f);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		b2body = agency.getWorld().createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0.01f;	// (default is 0.2f)
		CFBitSeq catBits = CommonCF.NO_CONTACT_CFCAT;
		CFBitSeq maskBits = CommonCF.NO_CONTACT_CFMASK;
		if(isContactEnabled) {
			catBits = MAINBODY_CFCAT;
			maskBits = MAINBODY_CFMASK;
		}
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, fdef, null, catBits, maskBits,
				getBodySize().x, getBodySize().y);
	}

	// "bottom" sensors
	private void createGroundAndPipeSensor(boolean isBig, boolean isDucking) {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();

		// foot sensor for detecting onGround and warp pipes
		if(!isBig || isDucking)
			boxShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(2f), new Vector2(0f, UInfo.P2M(-6)), 0f);
		else
			boxShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(2f), new Vector2(0f, UInfo.P2M(-16)), 0f);
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

	private void createSidePipeSensors() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();

		// right side sensor for detecting warp pipes
		boxShape.setAsBox(UInfo.P2M(1f), UInfo.P2M(5f), UInfo.P2MVector(7, 0), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		rightSideSensorFixture = b2body.createFixture(fdef);
		rightSideSensorFixture.setUserData(new AgentBodyFilter(SIDE_PIPE_SENSOR_CFCAT,
				SIDE_PIPE_SENSOR_CFMASK, wpSensor));

		// left side sensor for detecting warp pipes
		boxShape.setAsBox(UInfo.P2M(1f), UInfo.P2M(5f), UInfo.P2MVector(-7, 0), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = CommonCF.NO_CONTACT_CFCAT;
		CFBitSeq maskBits = CommonCF.NO_CONTACT_CFMASK;
		if(isContactEnabled) {
			catBits = SIDE_PIPE_SENSOR_CFCAT;
			maskBits = SIDE_PIPE_SENSOR_CFMASK;
		}
		leftSideSensorFixture = b2body.createFixture(fdef);
		leftSideSensorFixture.setUserData(new AgentBodyFilter(catBits, maskBits, wpSensor));
	}

	// "top" sensors
	private void createBumpTileAndPipeSensor(boolean isBig, boolean isDucking) {
		FixtureDef fdef = new FixtureDef();
		PolygonShape sensorShape = new PolygonShape();

		// head sensor for detecting head banging behavior
		if(!isBig || isDucking)
			sensorShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(1f), new Vector2(UInfo.P2M(0f), UInfo.P2M(8f)), 0f);
		else
			sensorShape.setAsBox(UInfo.P2M(5f), UInfo.P2M(1f), new Vector2(UInfo.P2M(0f), UInfo.P2M(16f)), 0f);
		fdef.shape = sensorShape;
		fdef.isSensor = true;
		btSensor = new AgentContactHoldSensor(this);
		btSensor.chainTo(wpSensor);
		CFBitSeq catBits = CommonCF.NO_CONTACT_CFCAT;
		CFBitSeq maskBits = CommonCF.NO_CONTACT_CFMASK;
		if(isContactEnabled) {
			catBits = BUMPTILE_AND_PIPE_SENSOR_CFCAT;
			maskBits = BUMPTILE_AND_PIPE_SENSOR_CFMASK;
		}
		topSensorFixture = b2body.createFixture(fdef);
		topSensorFixture.setUserData(new AgentBodyFilter(catBits, maskBits, btSensor));
	}

	private void createAgentSensor() {
		PolygonShape boxShape = new PolygonShape();
		Vector2 bs = getBodySize();
		boxShape.setAsBox(bs.x/2f, bs.y/2f);
		FixtureDef fdef = new FixtureDef();
		fdef.shape = boxShape;
		fdef.isSensor = true;
		acSensor = new AgentContactHoldSensor(this);
		acBeginSensor = new AgentContactBeginSensor(this);
		acBeginSensor.chainTo(acSensor);
		CFBitSeq catBits = NOCONTACT_AS_CFCAT;
		CFBitSeq maskBits = NOCONTACT_AS_CFMASK;
		if(isContactEnabled) {
			catBits = AS_CFCAT;
			maskBits = AS_CFMASK;
		}
		agentSensorFixture = b2body.createFixture(fdef);
		agentSensorFixture.setUserData(new AgentBodyFilter(catBits, maskBits, acBeginSensor));
	}

	public void decelLeftRight() {
		float vx = b2body.getLinearVelocity().x;
		if(vx == 0f)
			return;

		if(vx > 0f)
			b2body.applyLinearImpulse(new Vector2(-DECEL_XIMP, 0f), b2body.getWorldCenter(), true);
		else if(vx < 0f)
			b2body.applyLinearImpulse(new Vector2(DECEL_XIMP, 0f), b2body.getWorldCenter(), true);

		// do not decel so hard he moves in opposite direction
		if((vx > 0f && b2body.getLinearVelocity().x < 0f) || (vx < 0f && b2body.getLinearVelocity().x > 0f))
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
	}

	public void moveBodyLeftRight(boolean right, boolean doRunRun, boolean isDucking) {
		float speed;
		float max;
		if(ogSensor.isOnGround())
			speed = doRunRun ? MARIO_RUNMOVE_XIMP : MARIO_WALKMOVE_XIMP;
		else {
			speed = MARIO_AIRMOVE_XIMP;
			if(isDucking)
				speed /= 2f;
		}
		if(doRunRun)
			max = MARIO_MAX_RUNVEL;
		else
			max = MARIO_MAX_WALKVEL;
		if(right && b2body.getLinearVelocity().x <= max)
			b2body.applyLinearImpulse(new Vector2(speed, 0f), b2body.getWorldCenter(), true);
		else if(!right && b2body.getLinearVelocity().x >= -max)
			b2body.applyLinearImpulse(new Vector2(-speed, 0f), b2body.getWorldCenter(), true);
	}

	public void brakeLeftRight(boolean right) {
		float vx = b2body.getLinearVelocity().x;
		if(vx == 0f)
			return;

		if(right && vx < 0f)
			b2body.applyLinearImpulse(new Vector2(MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);
		else if(!right && vx > 0f)
			b2body.applyLinearImpulse(new Vector2(-MARIO_BRAKE_XIMP, 0f), b2body.getWorldCenter(),  true);

		// do not brake so hard he moves in opposite direction
		if((vx > 0f && b2body.getLinearVelocity().x < 0f) || (vx < 0f && b2body.getLinearVelocity().x > 0f))
			b2body.setLinearVelocity(0f, b2body.getLinearVelocity().y);
	}

	public void duckSlideLeftRight(boolean right) {
		if(right && b2body.getLinearVelocity().x <= MARIO_MAX_DUCKSLIDE_VEL)
			b2body.applyLinearImpulse(new Vector2(MARIO_DUCKSLIDE_XIMP, 0f), b2body.getWorldCenter(), true);
		else if(!right && b2body.getLinearVelocity().x >= -MARIO_MAX_DUCKSLIDE_VEL)
			b2body.applyLinearImpulse(new Vector2(-MARIO_DUCKSLIDE_XIMP, 0f), b2body.getWorldCenter(), true);
	}

	public boolean isOnGround() {
		return ogSensor.isOnGround();
	}

	public void useScriptedBodyState(ScriptedBodyState sbState, boolean isBig, boolean isDucking) {
		setContactEnabled(sbState.contactEnabled);
		if(!sbState.position.epsilonEquals(b2body.getPosition(), POSITION_EPS))
			defineBody(sbState.position, new Vector2(0f, 0f), isBig, isDucking);
		b2body.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
	}

	public void setContactEnabled(boolean enabled) {
		// exit if no change necessary
		if((isContactEnabled && enabled) || (!isContactEnabled && !enabled))
			return;
		if(enabled) {
			// enable contacts
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = MAINBODY_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = MAINBODY_CFMASK;
			((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = AS_CFCAT;
			((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = AS_CFMASK;
			((AgentBodyFilter) topSensorFixture.getUserData()).categoryBits = BUMPTILE_AND_PIPE_SENSOR_CFCAT;
			((AgentBodyFilter) topSensorFixture.getUserData()).maskBits = BUMPTILE_AND_PIPE_SENSOR_CFMASK;
			((AgentBodyFilter) rightSideSensorFixture.getUserData()).categoryBits = SIDE_PIPE_SENSOR_CFCAT;
			((AgentBodyFilter) rightSideSensorFixture.getUserData()).maskBits = SIDE_PIPE_SENSOR_CFMASK;
			((AgentBodyFilter) leftSideSensorFixture.getUserData()).categoryBits = SIDE_PIPE_SENSOR_CFCAT;
			((AgentBodyFilter) leftSideSensorFixture.getUserData()).maskBits = SIDE_PIPE_SENSOR_CFMASK;
			((AgentBodyFilter) ogSensorFixture.getUserData()).categoryBits = GROUND_AND_PIPE_SENSOR_CFCAT;
			((AgentBodyFilter) ogSensorFixture.getUserData()).maskBits = GROUND_AND_PIPE_SENSOR_CFMASK;
		}
		else {
			// disable contacts
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
			((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = NOCONTACT_AS_CFCAT;
			((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = NOCONTACT_AS_CFMASK;
			((AgentBodyFilter) topSensorFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) topSensorFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
			((AgentBodyFilter) rightSideSensorFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) rightSideSensorFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
			((AgentBodyFilter) leftSideSensorFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) leftSideSensorFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
			((AgentBodyFilter) ogSensorFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
			((AgentBodyFilter) ogSensorFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
		}
		// the contact filters were changed, so let Box2D know to update contacts here
		mainBodyFixture.refilter();
		topSensorFixture.refilter();
		rightSideSensorFixture.refilter();
		leftSideSensorFixture.refilter();
		agentSensorFixture.refilter();
		ogSensorFixture.refilter();
		// update the contacts enabled flag
		isContactEnabled = enabled;
	}

	public RoomBox getCurrentRoom() {
		return (RoomBox) acSensor.getFirstContactByClass(RoomBox.class);
	}

	public <T> T getFirstContactByClass(Class<T> cls) {
		return acSensor.getFirstContactByClass(cls);
	}

	public <T> List<T> getContactsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}

	public List<Agent> getAndResetBeginContacts() {
		return acBeginSensor.getAndResetContacts();
	}

	public List<TileBumpTakeAgent> getBumptileContacts() {
		return btSensor.getContactsByClass(TileBumpTakeAgent.class);
	}

	public PipeWarp getEnterPipeWarp(Direction4 moveDir) {
		if(moveDir == null)
			return null;
		for(PipeWarp pw : wpSensor.getContactsByClass(PipeWarp.class)) {
			if(pw.canBodyEnterPipe(getBounds(), moveDir))
				return (PipeWarp) pw;
		}
		return null;
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	@Override
	public Vector2 getPosition() {
		return b2body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		Vector2 s = getBodySize();
		return new Rectangle(b2body.getPosition().x - s.x/2f, b2body.getPosition().y - s.y/2f, s.x, s.y);
	}

	public void updatePrevs() {
		prevVelocity = b2body.getLinearVelocity().cpy();
		prevPosition = b2body.getPosition().cpy();
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}
}
