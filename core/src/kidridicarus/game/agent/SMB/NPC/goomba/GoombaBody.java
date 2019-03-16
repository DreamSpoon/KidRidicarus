package kidridicarus.game.agent.SMB.NPC.goomba;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class GoombaBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private static final CFBitSeq MAIN_ENABLED_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_ENABLED_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq MAIN_DISABLED_CFCAT = CommonCF.NO_CONTACT_CFCAT;
	private static final CFBitSeq MAIN_DISABLED_CFMASK = CommonCF.NO_CONTACT_CFMASK;

	private static final CFBitSeq AS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.DESPAWN_BIT);

	private Goomba parent;
	private GoombaSpine spine;
	private Fixture mainBodyFixture;
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
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, spine.createHorizontalMoveSensor(),
				MAIN_ENABLED_CFCAT, MAIN_ENABLED_CFMASK, getBodySize().x, getBodySize().y);
		// agent sensor fixture
		acSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK, BODY_WIDTH, BODY_HEIGHT);
		// ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -BODY_HEIGHT/2f));
	}

	public void setMainSolid(boolean enabled) {
		if(enabled) {
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = MAIN_ENABLED_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = MAIN_ENABLED_CFMASK;
		}
		else {
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = MAIN_DISABLED_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = MAIN_DISABLED_CFMASK;
		}
		mainBodyFixture.refilter();
	}

	public void setAgentSensorEnabled(boolean enabled) {
		if(enabled) {
			((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_ENABLED_CFCAT;
			((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_ENABLED_CFMASK;
		}
		else {
			((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
			((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		}
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
