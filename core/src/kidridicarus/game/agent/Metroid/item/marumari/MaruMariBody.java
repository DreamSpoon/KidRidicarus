package kidridicarus.game.agent.Metroid.item.marumari;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.PowerupSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MaruMariBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(4f);
	private static final float BODY_HEIGHT = UInfo.P2M(4f);

	private MaruMari parent;
	private PowerupSpine spine;

	public MaruMariBody(MaruMari parent, World world, Vector2 position) {
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
		b2body.setGravityScale(0f);

		spine = new PowerupSpine(this);
	}

	private void createFixtures() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public PowerupSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
