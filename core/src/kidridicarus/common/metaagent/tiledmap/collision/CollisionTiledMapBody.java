package kidridicarus.common.metaagent.tiledmap.collision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class CollisionTiledMapBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.COLLISIONMAP_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	public CollisionTiledMapBody(CollisionTiledMapAgent parent, World world, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	// TODO what if body is redefined? how to delete all the old stuff and move it? 
	@Override
	protected void defineBody(Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		B2DFactory.makeSensorBoxFixture(b2body, this, CFCAT_BITS, CFMASK_BITS, bounds.width, bounds.height);
	}
}
