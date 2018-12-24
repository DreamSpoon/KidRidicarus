package kidridicarus.agent.bodies.SMB.enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contacts.CFBitSeq.CFBit;
import kidridicarus.agency.contacts.AgentBodyFilter;
import kidridicarus.agency.contacts.CFBitSeq;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.enemy.Turtle;
import kidridicarus.agent.bodies.MobileGroundAgentBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.agent.bodies.optional.BumpableBody;
import kidridicarus.agent.bodies.sensor.WalkingSensor;
import kidridicarus.agent.bodies.sensor.WalkingSensor.WalkingSensorType;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.UInfo;

public class TurtleBody extends MobileGroundAgentBody implements AgentContactBody, BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private Turtle parent;

	public TurtleBody(Turtle parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position, new Vector2(0f, 0f));
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT, CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT, CFBit.AGENT_BIT, CFBit.GUIDE_SENSOR_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
		createBottomSensorFixture();
	}

	private void createBottomSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footShape = new PolygonShape();
		footShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.isSensor = true;
		fdef.shape = footShape;
		CFBitSeq catBits = new CFBitSeq(CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits,
				new WalkingSensor(this, WalkingSensorType.FOOT)));
	}

	@Override
	public void onContactWall(LineSeg seg) {
		parent.onContactBoundLine(seg);
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		parent.onBump(bumpingAgent);
	}

	@Override
	public void onContactAgent(AgentBody agentBody) {
		parent.onContactAgent(agentBody.getParent());
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
