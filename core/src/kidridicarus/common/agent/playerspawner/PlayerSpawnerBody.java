package kidridicarus.common.agent.playerspawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class PlayerSpawnerBody extends AgentBody {
	public PlayerSpawnerBody(World world, PlayerSpawner parent, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		B2DFactory.makeSensorBoxFixture(b2body, this, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK,
				bounds.width, bounds.height);
	}
}
