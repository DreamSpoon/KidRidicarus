package kidridicarus.agent.bodies.SMB.enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agencydirector.WalkingSensor;
import kidridicarus.agencydirector.WalkingSensor.WalkingSensorType;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.enemy.Goomba;
import kidridicarus.agent.bodies.MobileGroundAgentBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.agent.bodies.optional.BumpableBody;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class GoombaBody extends MobileGroundAgentBody implements AgentContactBody, BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private Goomba parent;

	public GoombaBody(Goomba parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, GameInfo.AGENT_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.AGENT_BIT | GameInfo.GUIDE_SENSOR_BIT), position,
				BODY_WIDTH, BODY_HEIGHT);
		createBottomSensorFixture();
	}

	// create the foot sensor for detecting onGround
	private void createBottomSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footSensor;
		footSensor = new PolygonShape();
		footSensor.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.filter.categoryBits = GameInfo.AGENT_SENSOR_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		fdef.shape = footSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new WalkingSensor(this, WalkingSensorType.FOOT));
	}

	@Override
	public void onContactWall(LineSeg seg) {
		parent.onContactBoundLine(seg);
	}

	@Override
	public void onBump(Agent bumpingAgent, Vector2 fromCenter) {
		parent.onBump(bumpingAgent, fromCenter);
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
