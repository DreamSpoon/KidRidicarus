package kidridicarus.game.agent.SMB.NPC.turtle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class TurtleBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT);

	private Turtle parent;
	private TurtleSpine spine;

	public TurtleBody(Turtle parent, World world, Vector2 position) {
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
		spine = new TurtleSpine(this);
	}

	private void createFixtures() {
		// create main fixture
		B2DFactory.makeBoxFixture(b2body, spine.createHorizontalMoveSensor(),
				MAIN_CFCAT, MAIN_CFMASK, getBodySize().x, getBodySize().y);
		// create agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentContactSensor(),
				AS_CFCAT, AS_CFMASK, getBodySize().x, getBodySize().y);
		// create ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK,
				FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f));
	}

	public TurtleSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
