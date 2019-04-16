package kidridicarus.common.agent.levelendtrigger;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class LevelEndTriggerBody extends AgentBody {
	private OneWayContactSensor playerSensor;

	public LevelEndTriggerBody(LevelEndTrigger parent, World world, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBodySize(bounds.width, bounds.height);
		createBody(world, bounds);
		createFixtures();
	}

	private void createBody(World world, Rectangle bounds) {
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
	}

	private void createFixtures() {
		playerSensor = new OneWayContactSensor(this, true);
		B2DFactory.makeBoxFixture(b2body, CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK, playerSensor,
				getBodySize().x, getBodySize().y);
	}

	public List<PlayerAgent> getPlayerBeginContacts() {
		return playerSensor.getOnlyAndResetContacts(PlayerAgent.class);
	}
}
