package kidridicarus.game.agent.SMB1.item.mushroom;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class BaseMushroomBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private SolidContactSpine spine;

	public BaseMushroomBody(BaseMushroom parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		spine = new SolidContactSpine(this);
		createFixtures();
	}

	private void createFixtures() {
		SolidContactSensor solidSensor = spine.createSolidContactSensor();
		// create main fixture with agent sensor chained to horizontal move sensor
		B2DFactory.makeBoxFixture(b2body, solidSensor, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				getBodySize().x, getBodySize().y);
		// create on ground sensor fixture and attach to spine
		B2DFactory.makeSensorBoxFixture(b2body, solidSensor, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBodySize().y/2f));
		// create agent sensor
		AgentContactHoldSensor agentSensor = spine.createAgentSensor();
		B2DFactory.makeSensorBoxFixture(b2body, agentSensor, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
