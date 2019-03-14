package kidridicarus.game.agent.SMB.NPC.goomba;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class GoombaBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

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
//		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, hmSensor, CommonCF.SOLID_BODY_CFCAT,
//				CommonCF.SOLID_BODY_CFMASK, position, BODY_WIDTH, BODY_HEIGHT);
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		b2body = world.createBody(bdef);

		spine = new GoombaSpine(this);
	}

	private void createFixtures() {
		createMainFixture();
		createAgentSensorFixture();
		createGroundSensorFixture();
	}

	private void createMainFixture() {
		FixtureDef fdef = new FixtureDef();
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createHorizontalMoveSensor(),
				CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, getBodySize().x, getBodySize().y);
	}

	private void createAgentSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		acSensorFixture = b2body.createFixture(fdef);
		acSensorFixture.setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, spine.createAgentSensor()));
	}

	private void createGroundSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.GROUND_SENSOR_CFCAT,
				CommonCF.GROUND_SENSOR_CFMASK, spine.createOnGroundSensor()));
	}

	// disable contacts between the agent contact sensor and agents
	public void disableAgentContact() {
		if(!(acSensorFixture.getUserData() instanceof AgentBodyFilter))
			return;
		((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = CommonCF.NO_CONTACT_CFCAT;
		((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = CommonCF.NO_CONTACT_CFMASK;
		// the contact filters were changed, so let Box2D know to update contacts here
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
