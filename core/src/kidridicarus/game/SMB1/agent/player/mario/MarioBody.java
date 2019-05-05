package kidridicarus.game.SMB1.agent.player.mario;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.agency.agentscript.ScriptedBodyState;
import kidridicarus.common.agent.playeragent.PlayerAgentBody;
import kidridicarus.common.agentbrain.BrainContactFrameInput;
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
	private static final CFBitSeq MAIN_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.SCROLL_PUSH_BIT, CommonCF.Alias.SEMISOLID_FLOOR_BIT);
	// agent sensor
	private static final CFBitSeq AS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.ROOM_BIT, CommonCF.Alias.SOLID_MAP_BIT, CommonCF.Alias.POWERUP_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.SCROLL_KILL_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.SOLID_MAP_BIT);
	private static final CFBitSeq TILEBUMP_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq TILEBUMP_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq PIPEWARP_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq PIPEWARP_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.PIPEWARP_BIT);

	private static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT,
			CommonCF.Alias.SEMISOLID_FLOOR_FOOT_BIT);

	private static final float FRICTION = 0f;
	private static final float GRAVITY_SCALE = 2f;

	private MarioSpine spine;
	private Fixture agentSensorFixture;
	private boolean isAgentSensorEnabled;
	private boolean isBigBody;
	private boolean isDucking;

	public MarioBody(Mario parent, World world, Vector2 position, Vector2 velocity, boolean isBigBody,
			boolean isDucking) {
		super(parent, world, position, velocity);
		isAgentSensorEnabled = true;
		setMarioBodyStuff(position, velocity, isBigBody, isDucking);
	}

	public BrainContactFrameInput processContactFrame() {
		return new BrainContactFrameInput(spine.getCurrentRoom(), spine.isContactKeepAlive(),
				spine.isContactDespawn());
	}

	public void setMarioBodyStuff(Vector2 position, Vector2 velocity, boolean isBigBody, boolean isDucking) {
		this.isBigBody = isBigBody;
		this.isDucking = isDucking;
		defineBody(new Rectangle(position.x, position.y, 0f, 0f), velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		if(isBigBody && !isDucking)
			setBoundsSize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBoundsSize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);
		createBody(bounds.getCenter(new Vector2()), velocity);
		createFixtures();
		resetPrevValues();
	}

	private void createBody(Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, MAIN_CFCAT, MAIN_CFMASK, this, getBounds().width, getBounds().height);
		spine = new MarioSpine(this);
	}

	private void createFixtures() {
		// create fixture for agent contact and damage push sensors
		AgentContactHoldSensor agentSensor = spine.createAgentSensor();
		agentSensor.chainTo(spine.createDamagePushSensor());
		if(isAgentSensorEnabled) {
			agentSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, AS_ENABLED_CFCAT, AS_ENABLED_CFMASK,
					agentSensor, getBounds().width, getBounds().height);
		}
		else {
			agentSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, AS_DISABLED_CFCAT, AS_DISABLED_CFMASK,
					agentSensor, getBounds().width, getBounds().height);
		}
		// create fixture for ground sensor
		B2DFactory.makeSensorBoxFixture(b2body, GROUND_SENSOR_CFCAT, GROUND_SENSOR_CFMASK,
				spine.createSolidContactSensor(), FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBounds().height/2f));
		// create fixture for tilebump sensor
		B2DFactory.makeSensorBoxFixture(b2body, TILEBUMP_SENSOR_CFCAT, TILEBUMP_SENSOR_CFMASK,
				spine.createTileBumpPushSensor(), HEAD_WIDTH, HEAD_HEIGHT, new Vector2(0f, getBounds().height/2f));

		AgentContactHoldSensor pwSensor = spine.createPipeWarpSensor();
		// create fixture for bottom pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK, pwSensor,
				TOPBOT_PW_SENSOR_WIDTH, TOPBOT_PW_SENSOR_HEIGHT, new Vector2(0f, -getBounds().height/2f));
		// create fixture for top pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK, pwSensor,
				TOPBOT_PW_SENSOR_WIDTH, TOPBOT_PW_SENSOR_HEIGHT, new Vector2(0f, getBounds().height/2f));
		// create fixture for left pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK, pwSensor,
				SIDE_PW_SENSOR_WIDTH, SIDE_PW_SENSOR_HEIGHT, new Vector2(-getBounds().width/2f, 0f));
		// create fixture for right pipewarp sensor
		B2DFactory.makeSensorBoxFixture(b2body, PIPEWARP_SENSOR_CFCAT, PIPEWARP_SENSOR_CFMASK, pwSensor,
				SIDE_PW_SENSOR_WIDTH, SIDE_PW_SENSOR_HEIGHT, new Vector2(getBounds().width/2f, 0f));
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
		if(!sbState.position.epsilonEquals(b2body.getPosition(), UInfo.POS_EPSILON)) {
			this.isBigBody = bigBody;
			this.isDucking = false;
			defineBody(new Rectangle(sbState.position.x, sbState.position.y, 0f, 0f), new Vector2(0f, 0f));
		}
		b2body.setGravityScale(sbState.gravityFactor * GRAVITY_SCALE);
		// Body may "fall asleep" while no activity, also while gravityScale was zero,
		// wake it up so that gravity functions again.
		b2body.setAwake(true);
	}
}
