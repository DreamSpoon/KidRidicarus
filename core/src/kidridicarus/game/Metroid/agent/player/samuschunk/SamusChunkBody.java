package kidridicarus.game.Metroid.agent.player.samuschunk;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SamusChunkBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(8);
	private static final float BODY_HEIGHT = UInfo.P2M(8);
	private static final float GRAVITY_SCALE = 0.25f;

	public SamusChunkBody(Agent parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(new Rectangle(position.x, position.y, 0f, 0f));
	}

	@Override
	public void defineBody(Rectangle bounds, Vector2 velocity) {
		if(b2body != null)
			world.destroyBody(b2body);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				BODY_WIDTH, BODY_HEIGHT);
	}
}
