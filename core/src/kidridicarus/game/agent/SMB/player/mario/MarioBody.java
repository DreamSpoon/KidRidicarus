package kidridicarus.game.agent.SMB.player.mario;

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

public class MarioBody extends PlayerAgentBody {
	private static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	private static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));
	private static final float FOOT_WIDTH = UInfo.P2M(5f);
	private static final float FOOT_HEIGHT = UInfo.P2M(2f);
	// TODO head size needs some work, bump tile is inconsistent
	private static final float HEAD_WIDTH = UInfo.P2M(10f);
	private static final float HEAD_HEIGHT = UInfo.P2M(12f);

	private static final float TOPBOT_PW_SENSOR_WIDTH = UInfo.P2M(5f);
	private static final float TOPBOT_PW_SENSOR_HEIGHT = UInfo.P2M(2f);
	private static final float SIDE_PW_SENSOR_WIDTH = UInfo.P2M(2f);
	private static final float SIDE_PW_SENSOR_HEIGHT = UInfo.P2M(5f);

	// main body
	private static final CFBitSeq MAIN_CFCAT = new CFBitSeq(Alias.AGENT_BIT);
	private static final CFBitSeq MAIN_CFMASK =
			new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT, CommonCF.Alias.SCROLL_PUSH_BIT);
	// agent sensor
	private static final CFBitSeq AS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.COLLISIONMAP_BIT, CommonCF.Alias.POWERUP_BIT,
			CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.COLLISIONMAP_BIT);
	private static final CFBitSeq TILEBUMP_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq TILEBUMP_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq PIPEWARP_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq PIPEWARP_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.PIPEWARP_BIT);

	private static final float FRICTION = 0f;
	private static final float GRAVITY_SCALE = 2f;

	private World world;
	private MarioSpine spine;
	private Fixture agentSensorFixture;
	private boolean isAgentSensorEnabled;

	public MarioBody(Mario parent, World world, Vector2 position, Vector2 velocity, boolean isBigBody,
			boolean isDucking) {
		super(parent, position, velocity);

		this.world = world;
		isAgentSensorEnabled = true;
		defineBody(position, velocity, isBigBody, isDucking);
	}

	public void defineBody(Vector2 position, Vector2 velocity, boolean isBigBody, boolean isDucking) {
		if(isBigBody && !isDucking)
			setBodySize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBodySize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);

		createBody(position, velocity);
		createFixtures();
	}

	private void createBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		resetPrevValues(position, velocity);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, this, MAIN_CFCAT, MAIN_CFMASK, getBodySize().x, getBodySize().y);
		spine = new MarioSpine(this);
	}

	private void createFixtures() {
		// create fixture for agent contact and damage push sensors
		AgentContactHoldSensor agentSensor = spine.createAgentSensor();
		agentSensor.chainTo(spine.createDamagePushSensor());
		if(isAgentSensorEnabled) {
			agentSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, agentSensor,
					AS_ENABLED_CFCAT, AS_ENABLED_CFMASK, getBodySize().x, getBodySize().y);
		}
		else {
			agentSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, agentSensor,
					AS_DISABLED_CFCAT, AS_DISABLED_CFMASK, getBodySize().x, getBodySize().y);
		}
		// create fixture for ground sensor
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBodySize().y/2f));
		// create fixture for tilebump sensor
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

	@Override
	public void applyImpulse (Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}

	public void allowOnlyDeadContacts() {
		// disable all, and...
		disableAllContacts();
		// ... re-enable the needed agent contact sensor bits
		((AgentBodyFilter) agentSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
		((AgentBodyFilter) agentSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		agentSensorFixture.refilter();
		isAgentSensorEnabled = false;
	}

	public MarioSpine getSpine() {
		return spine;
	}

	public void useScriptedBodyState(ScriptedBodyState sbState, boolean bigBody) {
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
		if(!sbState.position.epsilonEquals(b2body.getPosition(), UInfo.POS_EPSILON))
			defineBody(sbState.position, new Vector2(0f, 0f), bigBody, false);
		b2body.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
	}
}
