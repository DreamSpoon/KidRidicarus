package kidridicarus.common.metaagent.tiledmap.solidlayer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class SolidTiledMapBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SOLID_MAP_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	public SolidTiledMapBody(SolidTiledMapAgent parent, World world, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	// TODO what if body is redefined? how to delete all the old stuff and move it?
	// Also, velocity is ignored.
	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		B2DFactory.makeSensorBoxFixture(b2body, CFCAT_BITS, CFMASK_BITS, this, bounds.width, bounds.height);
	}
}
