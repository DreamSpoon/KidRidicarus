package kidridicarus.common.agent.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

class PlayerSpawnerBody extends AgentBody {
	private static final float GRAVITY_SCALE = 0f;

	PlayerSpawnerBody(World world, PlayerSpawner parent, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
				bounds.width, bounds.height);
	}
}
