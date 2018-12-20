package kidridicarus.agent.bodies.SMB.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agencydirector.AgentSensor;
import kidridicarus.agencydirector.AgentSensor.AgentSensorType;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.item.BaseMushroom;
import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.agent.bodies.optional.BumpableBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;

public class BaseMushroomBody extends MobileAgentBody implements BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private BaseMushroom parent;

	public BaseMushroomBody(BaseMushroom parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, GameInfo.ITEM_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.GUIDE_SENSOR_BIT), position, BODY_WIDTH, BODY_HEIGHT);
		createBottomSensor();
	}

	private void createBottomSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footSensor = new PolygonShape();
		footSensor.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = footSensor;
		fdef.isSensor = true;
		fdef.filter.categoryBits = GameInfo.AGENT_SENSOR_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		b2body.createFixture(fdef).setUserData(new AgentSensor(this, AgentSensorType.FOOT));
	}

	@Override
	protected void onContactWall(LineSeg seg) {
		parent.onContactVertBoundLine(seg);
	}

	@Override
	public void onBump(Agent bumpingAgent, Vector2 fromCenter) {
		parent.onBump(bumpingAgent, fromCenter);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
