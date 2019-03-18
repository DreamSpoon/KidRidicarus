package kidridicarus.game.agent.Metroid.NPC.skreeshot;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SkreeShotBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(6);
	private static final float BODY_HEIGHT = UInfo.P2M(6);

	private SkreeShot parent;
	private AgentContactBeginSensor acSensor;

	public SkreeShotBody(SkreeShot parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		createBody(world, position, velocity);
		createFixtures();
	}

	private void createBody(World world, Vector2 position, Vector2 velocity) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 0f;
		b2body = world.createBody(bdef);
	}

	private void createFixtures() {
		acSensor = new AgentContactBeginSensor(this);
		B2DFactory.makeSensorBoxFixture(b2body, acSensor, CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	// check for and return list of begin contact agents that are ContactDmgTakeAgents 
	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return acSensor.getOnlyAndResetContacts(ContactDmgTakeAgent.class);
	}
}
