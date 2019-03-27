package kidridicarus.game.agent.Metroid.NPC.skreeshot;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SkreeShotBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(6);
	private static final float BODY_HEIGHT = UInfo.P2M(6);

	private OneWayContactSensor agentSensor;

	public SkreeShotBody(SkreeShot parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(position, velocity);
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(0f);
		// agent sensor fixture TODO why not use spine agent sensor? refactor this!
		agentSensor = new OneWayContactSensor(this, true);
		B2DFactory.makeSensorBoxFixture(b2body, agentSensor,
				CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	// check for and return list of begin contact agents that are ContactDmgTakeAgents 
	public List<ContactDmgTakeAgent> getContactDmgTakeAgents() {
		return agentSensor.getOnlyAndResetContacts(ContactDmgTakeAgent.class);
	}
}
