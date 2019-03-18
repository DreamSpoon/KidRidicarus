package kidridicarus.game.agent.Metroid.player.samus;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SamusShotBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(1);
	private static final float BODY_HEIGHT = UInfo.P2M(1);
	private static final float SENSOR_WIDTH = UInfo.P2M(3);
	private static final float SENSOR_HEIGHT = UInfo.P2M(3);

	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT);

	private SamusShot parent;
	private SolidBoundSensor boundSensor;
	private AgentContactHoldSensor acSensor;

	public SamusShotBody(SamusShot parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		createBody(world, position, velocity);
		createFixtures();
	}

	private void createBody(World world, Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
	}

	private void createFixtures() {
		// create main fixture
		boundSensor = new SolidBoundSensor(parent);
		B2DFactory.makeBoxFixture(b2body, boundSensor, MAIN_CFCAT, MAIN_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
		// create agent contact sensor fixture
		acSensor = new AgentContactHoldSensor(this);
		B2DFactory.makeSensorBoxFixture(b2body, acSensor, AS_CFCAT, AS_CFMASK,
				SENSOR_WIDTH, SENSOR_HEIGHT);
	}

	public boolean isHitBound() {
		return !boundSensor.getContacts().isEmpty();
	}

	public <T> List<T> getContactAgentsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
