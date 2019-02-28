package kidridicarus.game.SMB.agentbody.NPC;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.AgentContactSensor;
import kidridicarus.common.agentbody.sensor.OnGroundSensor;
import kidridicarus.common.agentbody.sensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.game.SMB.agent.NPC.Goomba;

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
		hmSensor = new SolidBoundSensor(parent);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, hmSensor, CommonCF.SOLID_BODY_CFCAT,
				CommonCF.SOLID_BODY_CFMASK, position, BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		acSensor = new AgentContactSensor(this);
		acSensorFixture = b2body.createFixture(fdef);
		acSensorFixture.setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, acSensor));
	}

	// create the foot sensor for detecting onGround
	private void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		ogSensor = new OnGroundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.GROUND_SENSOR_CFCAT,
				CommonCF.GROUND_SENSOR_CFMASK, ogSensor));
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
