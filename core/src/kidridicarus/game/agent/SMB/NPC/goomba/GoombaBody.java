package kidridicarus.game.agent.SMB.NPC.goomba;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class GoombaBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT);

	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.DESPAWN_BIT);

	private Goomba parent;
	private GoombaSpine spine;
	private Fixture acSensorFixture;

	public GoombaBody(Goomba parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createFixtures();
	}

	private void createBody(World world, Vector2 position) {
		b2body = B2DFactory.makeDynamicBody(world, position);
		spine = new GoombaSpine(this);
	}

	private void createFixtures() {
		// main body fixture
		B2DFactory.makeBoxFixture(b2body, spine.createHorizontalMoveSensor(), MAIN_CFCAT, MAIN_CFMASK,
				getBodySize().x, getBodySize().y);
		// agent sensor fixture
		AgentContactHoldSensor sensor = spine.createAgentContactSensor();
		sensor.chainTo(spine.createHeadBounceAndContactDamageSensor());
		acSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, sensor, AS_CFCAT, AS_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
		// ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -BODY_HEIGHT/2f));
	}

	public void allowOnlyDeadSquishContacts() {
		// change the needed agent contact sensor bits
		((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
		((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		acSensorFixture.refilter();
	}

	public void allowOnlyDeadBumpContacts() {
		disableAllContacts();
		// change the needed agent contact sensor bits
		((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
		((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		acSensorFixture.refilter();
	}

	public GoombaSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
