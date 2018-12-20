package kidridicarus.agent.bodies.SMB.enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.enemy.Turtle;
import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.agent.bodies.optional.AgentContactBody;
import kidridicarus.agent.bodies.optional.BumpableBody;
import kidridicarus.agent.bodies.optional.GroundCheckBody;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class TurtleBody extends MobileAgentBody implements GroundCheckBody, AgentContactBody, BumpableBody {
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
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, GameInfo.AGENT_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.AGENT_BIT | GameInfo.GUIDE_AGENTSENSOR_BIT),
				position, BODY_WIDTH, BODY_HEIGHT);
		createBottomSensorFixture();
	}

	private void createBottomSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footSensor = new PolygonShape();
		footSensor.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.filter.categoryBits = GameInfo.AGENTFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		fdef.isSensor = true;
		fdef.shape = footSensor;
		b2body.createFixture(fdef).setUserData(this);
	}

	@Override
	public void onContactVertBoundLine(LineSeg seg) {
		parent.onContactBoundLine(seg);
	}

	@Override
	public void onBump(Agent perp, Vector2 fromCenter) {
		parent.onBump(perp, fromCenter);
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
