package kidridicarus.game.agent.SMB.item.mushroom;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class BaseMushroomBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private BaseMushroom parent;
	private WalkPowerupSpine spine;

	public BaseMushroomBody(BaseMushroom parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createFixtures();
	}

	private void createBody(World world, Vector2 position) {
		b2body = B2DFactory.makeDynamicBody(world, position);
		spine = new WalkPowerupSpine(this);
	}

	private void createFixtures() {
		// create main fixture with agent sensor chained to horizontal move sensor
		AgentContactHoldSensor sensor = spine.createAgentSensor();
		sensor.chainTo(spine.createHMSensor());
		B2DFactory.makeBoxFixture(b2body, sensor,
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
		// create on ground sensor fixture and attach to spine
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK, FOOT_WIDTH, FOOT_HEIGHT,
				new Vector2(0f, -BODY_HEIGHT/2f));
	}

	public WalkPowerupSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
