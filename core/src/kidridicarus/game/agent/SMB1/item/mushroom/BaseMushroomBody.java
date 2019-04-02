package kidridicarus.game.agent.SMB1.item.mushroom;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class BaseMushroomBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private NPC_Spine spine;

	public BaseMushroomBody(BaseMushroom parent, World world, Vector2 position, Vector2 velocity) {
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
		spine = new NPC_Spine(this);
		createFixtures();
	}

	private void createFixtures() {
		// create main fixture with agent sensor chained to horizontal move sensor
		AgentContactHoldSensor sensor = spine.createAgentSensor();
		sensor.chainTo(spine.createHorizontalMoveSensor());
		B2DFactory.makeBoxFixture(b2body, sensor,
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
		// create on ground sensor fixture and attach to spine
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK, FOOT_WIDTH, FOOT_HEIGHT,
				new Vector2(0f, -BODY_HEIGHT/2f));
	}

	public NPC_Spine getSpine() {
		return spine;
	}
}
