package kidridicarus.game.agent.KidIcarus.player.pit;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.agency.agentscript.ScriptedBodyState;
import kidridicarus.common.agent.playeragent.PlayerAgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.CommonCF.Alias;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class PitBody extends PlayerAgentBody {
	private static final float STAND_BODY_WIDTH = UInfo.P2M(8f);
	private static final float STAND_BODY_HEIGHT = UInfo.P2M(16f);
	private static final float DUCKING_BODY_WIDTH = UInfo.P2M(8f);
	private static final float DUCKING_BODY_HEIGHT = UInfo.P2M(10f);
	private static final float FOOT_WIDTH = UInfo.P2M(4f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);
	private static final float GRAVITY_SCALE = 1f;
	private static final float FRICTION = 0f;	// (default is 0.2f)
	private static final Vector2 DUCK_TO_STAND_OFFSET = UInfo.P2MVector(0f, 3f);
	private static final float HEAD_WIDTH = UInfo.P2M(10f);
	private static final float HEAD_HEIGHT = UInfo.P2M(12f);
	private static final float TOPBOT_PW_SENSOR_WIDTH = UInfo.P2M(5f);
	private static final float TOPBOT_PW_SENSOR_HEIGHT = UInfo.P2M(2f);
	private static final float SIDE_PW_SENSOR_WIDTH = UInfo.P2M(2f);
	private static final float SIDE_PW_SENSOR_HEIGHT = UInfo.P2M(5f);

	// main body
	private static final CFBitSeq MAINBODY_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	private static final CFBitSeq MAINBODY_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.SCROLL_PUSH_BIT, CommonCF.Alias.SEMISOLID_FLOOR_BIT);
	// agent sensor
	private static final CFBitSeq AS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.ROOM_BIT, CommonCF.Alias.SOLID_MAP_BIT, CommonCF.Alias.POWERUP_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.SCROLL_KILL_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.SOLID_MAP_BIT);
	private static final CFBitSeq TILEBUMP_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq TILEBUMP_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq PIPEWARP_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq PIPEWARP_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.PIPEWARP_BIT);

	private static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.SEMISOLID_FLOOR_FOOT_BIT);

	private PitSpine spine;
	private boolean isAgentSensorEnabled;
	private Fixture agentSensorFixture;
	private boolean isDuckingForm;

	public PitBody(Pit parent, World world, Vector2 position, Vector2 velocity, boolean isDuckingForm) {
		super(parent, world, position, new Vector2(0f, 0f));
		isAgentSensorEnabled = true;
		this.isDuckingForm = isDuckingForm;
		defineBody(position, velocity);
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		if(isDuckingForm)
			setBodySize(DUCKING_BODY_WIDTH, DUCKING_BODY_HEIGHT);
		else
			setBodySize(STAND_BODY_WIDTH, STAND_BODY_HEIGHT);
		createBody(position, velocity);
		createFixtures();
	}

	private void createBody(Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		resetPrevValues(position, velocity);

		spine = new PitSpine(this);
	}

	private void createFixtures() {
		// create main fixture
		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createSolidBodySensor(), MAINBODY_CFCAT, MAINBODY_CFMASK,
				getBodySize().x, getBodySize().y);

		// create agent sensor fixture
		if(isAgentSensorEnabled) {
			agentSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
					AS_ENABLED_CFCAT, AS_ENABLED_CFMASK, getBodySize().x, getBodySize().y);
		}
		else {
			agentSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
					AS_DISABLED_CFCAT, AS_DISABLED_CFMASK, getBodySize().x, getBodySize().y);
		}
		// create on ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				GROUND_SENSOR_CFCAT, GROUND_SENSOR_CFMASK,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBodySize().y/2f));
		// create tilebump sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createTileBumpPushSensor(),
				TILEBUMP_SENSOR_CFCAT, TILEBUMP_SENSOR_CFMASK,
				HEAD_WIDTH, HEAD_HEIGHT, new Vector2(0f, getBodySize().y/2f));

		AgentContactHoldSensor pwSensor = spine.createPipeWarpSensor();
		// create fixture for bottom pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, pwSensor,
				PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK,
				TOPBOT_PW_SENSOR_WIDTH, TOPBOT_PW_SENSOR_HEIGHT, new Vector2(0f, -getBodySize().y/2f));
		// create fixture for top pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, pwSensor,
				PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK,
				TOPBOT_PW_SENSOR_WIDTH, TOPBOT_PW_SENSOR_HEIGHT, new Vector2(0f, getBodySize().y/2f));
		// create fixture for left pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, pwSensor,
				PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK,
				SIDE_PW_SENSOR_WIDTH, SIDE_PW_SENSOR_HEIGHT, new Vector2(-getBodySize().x/2f, 0f));
		// create fixture for right pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, pwSensor,
				PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK,
				SIDE_PW_SENSOR_WIDTH, SIDE_PW_SENSOR_HEIGHT, new Vector2(getBodySize().x/2f, 0f));
	}

	public void useScriptedBodyState(ScriptedBodyState sbState) {
		if(sbState.contactEnabled && !isAgentSensorEnabled) {
			((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = AS_ENABLED_CFCAT;
			((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = AS_ENABLED_CFMASK;
			agentSensorFixture.refilter();
			isAgentSensorEnabled = true;
		}
		else if(!sbState.contactEnabled && isAgentSensorEnabled) {
			((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
			((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
			agentSensorFixture.refilter();
			isAgentSensorEnabled = false;
		}
		if(!sbState.position.epsilonEquals(getPosition(), UInfo.POS_EPSILON))
			defineBody(sbState.position, new Vector2(0f, 0f));
		b2body.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
		if(sbState.gravityFactor != 0f ) {
			// Body may "fall asleep" while no activity, also while gravityScale was zero,
			// wake it up so that gravity functions again.
			b2body.setAwake(true);
		}
	}

	public void applyDead() {
		allowOnlyDeadContacts();
		zeroVelocity(true, true);
		b2body.setGravityScale(0f);
	}

	private void allowOnlyDeadContacts() {
		// disable all, and...
		disableAllContacts();
		// ... re-enable the needed agent contact sensor bits
		((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
		((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		agentSensorFixture.refilter();
		isAgentSensorEnabled = false;
	}

	public PitSpine getSpine() {
		return spine;
	}

	public void setDuckingForm(boolean isDuckingForm) {
		// if currently ducking and instructed to change to standing form...
		if(this.isDuckingForm && !isDuckingForm) {
			this.isDuckingForm = isDuckingForm;
			defineBody(b2body.getPosition().cpy().add(DUCK_TO_STAND_OFFSET), b2body.getLinearVelocity());
		}
		// if currently standing and instructed to change to ducking form...
		else if(!this.isDuckingForm && isDuckingForm) {
			this.isDuckingForm = isDuckingForm;
			defineBody(b2body.getPosition().cpy().sub(DUCK_TO_STAND_OFFSET), b2body.getLinearVelocity());
		}
	}

	public boolean isDuckingForm() {
		return isDuckingForm;
	}
}
