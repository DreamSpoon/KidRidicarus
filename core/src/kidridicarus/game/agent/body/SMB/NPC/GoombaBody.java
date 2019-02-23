package kidridicarus.game.agent.body.SMB.NPC;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.MobileAgentBody;
import kidridicarus.agency.agent.body.sensor.AgentContactSensor;
import kidridicarus.agency.agent.body.sensor.OnGroundSensor;
import kidridicarus.agency.agent.body.sensor.SolidBoundSensor;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.game.agent.SMB.NPC.Goomba;

public class GoombaBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private Goomba parent;
	private OnGroundSensor ogSensor;
	private SolidBoundSensor hmSensor;
	private AgentContactSensor acSensor;
	private Fixture acSensorFixture;

	public GoombaBody(Goomba parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createAgentSensor();
		createGroundSensor();
	}

	private void createBody(World world, Vector2 position) {
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		hmSensor = new SolidBoundSensor(parent);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, hmSensor, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT);
		acSensor = new AgentContactSensor(this);
		acSensorFixture = b2body.createFixture(fdef);
		acSensorFixture.setUserData(new AgentBodyFilter(catBits, maskBits, acSensor));
	}

	// create the foot sensor for detecting onGround
	private void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		ogSensor = new OnGroundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, ogSensor));
	}

	public boolean isMoveBlocked(boolean moveRight) {
		return hmSensor.isHMoveBlocked(getBounds(), moveRight);
	}

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}

	public boolean isMoveBlockedByAgent(boolean moveRight) {
		return AgentContactSensor.isMoveBlockedByAgent(acSensor, getPosition(), moveRight);
	}

	// disable contacts between the agent contact sensor and agents
	public void disableAgentContact() {
		if(!(acSensorFixture.getUserData() instanceof AgentBodyFilter))
			return;
		((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = new CFBitSeq();
		((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = new CFBitSeq();
		// the contact filters were changed, so let Box2D know to update contacts here
		acSensorFixture.refilter();
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
