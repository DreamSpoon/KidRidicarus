package kidridicarus.game.SMB1.agent.other.spincoin;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

class SpinCoinBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);

	SpinCoinBody(Agent parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x, position.y, 0f, 0f), velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				BODY_WIDTH, BODY_HEIGHT);
	}
}
