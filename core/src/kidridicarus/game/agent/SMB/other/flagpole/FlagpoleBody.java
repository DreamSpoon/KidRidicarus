package kidridicarus.game.agent.SMB.other.flagpole;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.PlayerAgent;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class FlagpoleBody extends AgentBody {
	private OneWayContactSensor agentBeginContactSensor;

	public FlagpoleBody(Flagpole parent, World world, Rectangle bounds) {
		super(parent);
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		createFixtures();
	}

	private void createFixtures() {
		agentBeginContactSensor = new OneWayContactSensor(this, true);
		B2DFactory.makeBoxFixture(b2body, agentBeginContactSensor,
				CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	public List<PlayerAgent> getPlayerBeginContacts() {
		return agentBeginContactSensor.getOnlyAndResetContacts(PlayerAgent.class);
	}
}
