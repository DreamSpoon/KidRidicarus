package kidridicarus.game.agent.SMB.NPC.turtle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.agent.SMB.BumpableBody;

public class TurtleBody extends MobileAgentBody implements BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

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
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		b2body = world.createBody(bdef);

		spine = new TurtleSpine(this);
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
		fdef.isSensor = true;
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createAgentContactSensor(),
				CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK, getBodySize().x, getBodySize().y);
	}

	private void createGroundSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK, FOOT_WIDTH/2f, FOOT_HEIGHT/2f,
				new Vector2(0f, -BODY_HEIGHT/2f));
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		parent.onBump(bumpingAgent);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	public TurtleSpine getSpine() {
		return spine;
	}
}
