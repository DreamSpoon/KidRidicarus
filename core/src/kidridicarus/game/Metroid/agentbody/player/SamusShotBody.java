package kidridicarus.game.Metroid.agentbody.player;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.common.agentbody.general.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.common.agentbody.sensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.Metroid.agent.player.SamusShot;

public class SamusShotBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(1);
	private static final float BODY_HEIGHT = UInfo.P2M(1);
	private static final float SENSOR_WIDTH = UInfo.P2M(3);
	private static final float SENSOR_HEIGHT = UInfo.P2M(3);

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
		createAgentSensor();
	}

	private void createBody(World world, Vector2 position, Vector2 velocity) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 0f;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		boundSensor = new SolidBoundSensor(parent);
		B2DFactory.makeBoxFixture(b2body, fdef, boundSensor, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(SENSOR_WIDTH/2f, SENSOR_HEIGHT/2f);
		fdef.isSensor = true;
		fdef.shape = boxShape;
		acSensor = new AgentContactHoldSensor(this);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, acSensor));
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
