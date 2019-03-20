package kidridicarus.game.agent.SMB.other.flagpole;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.AgentContactBeginSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class FlagpoleBody extends AgentBody {
	private AgentContactBeginSensor agentBeginContactSensor; 

	public FlagpoleBody(Flagpole parent, World world, Rectangle bounds) {
		super(parent);
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		createBody(world, bounds);
		createFixtures();
	}

	private void createBody(World world, Rectangle bounds) {
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
	}

	private void createFixtures() {
		agentBeginContactSensor = new AgentContactBeginSensor(this);
		B2DFactory.makeBoxFixture(b2body, agentBeginContactSensor,
				CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	public List<Agent> getPlayerBeginContacts() {
		return agentBeginContactSensor.getAndResetContacts();
	}
}
