package kidridicarus.game.agent.SMB.item.mushroom;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.agentspine.OnGroundSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.agent.SMB.BumpableBody;

public class BaseMushroomBody extends MobileAgentBody implements BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private BaseMushroom parent;
	private OnGroundSpine spine;
	private SolidBoundSensor hmSensor;

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
		hmSensor = new SolidBoundSensor(parent);
		b2body = B2DFactory.makeDynamicBody(world, position);
	}

	private void createFixtures() {
		// create main fixture
		B2DFactory.makeBoxFixture(b2body, new FixtureDef(), hmSensor,
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
		// create spine for detecting on ground only
		spine = new OnGroundSpine();
		// create on ground sensor fixture and attach to spine
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				CommonCF.GROUND_SENSOR_CFCAT, CommonCF.GROUND_SENSOR_CFMASK, FOOT_WIDTH, FOOT_HEIGHT,
				new Vector2(0f, -BODY_HEIGHT/2f));
	}

	public OnGroundSpine getSpine() {
		return spine;
	}

	public boolean isMoveBlocked(boolean movingRight) {
		return hmSensor.isHMoveBlocked(getBounds(), movingRight);
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		parent.onBump(bumpingAgent);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
