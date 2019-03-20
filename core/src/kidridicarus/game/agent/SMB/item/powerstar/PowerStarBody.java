package kidridicarus.game.agent.SMB.item.powerstar;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.agent.SMB.item.mushroom.WalkPowerupSpine;

public class PowerStarBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0.5f;

	private WalkPowerupSpine spine;

	public PowerStarBody(PowerStar parent, World world, Vector2 position) {
		super(parent);
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		createBody(world, position);
		createFixtures();
	}

	private void createBody(World world, Vector2 position) {
		b2body = B2DFactory.makeDynamicBody(world, position);
		b2body.setGravityScale(GRAVITY_SCALE);

		spine = new WalkPowerupSpine(this);
	}

	private void createFixtures() {
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		// create agent sensor and horizontal move sensor
		AgentContactHoldSensor sensor = spine.createAgentSensor();
		sensor.chainTo(spine.createHMSensor());
		B2DFactory.makeBoxFixture(b2body, fdef, sensor,
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public WalkPowerupSpine getSpine() {
		return spine;
	}
}
