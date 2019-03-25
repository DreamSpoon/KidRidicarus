package kidridicarus.game.agent.SMB.item.staticcoin;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.PowerupSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class StaticCoinBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16f);
	private static final float BODY_HEIGHT = UInfo.P2M(16f);

	private PowerupSpine spine;

	public StaticCoinBody(StaticCoin parent, World world, Vector2 position) {
		super(parent);
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeStaticBody(world, position);
		spine = new PowerupSpine(this);
		createFixtures();
	}

	private void createFixtures() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public PowerupSpine getSpine() {
		return spine;
	}
}
