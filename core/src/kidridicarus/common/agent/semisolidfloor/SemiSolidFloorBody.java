package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class SemiSolidFloorBody extends AgentBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SEMISOLID_FLOOR_BIT,
			CommonCF.Alias.SEMISOLID_FLOOR_FOOT_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	public SemiSolidFloorBody(Agent parent, World world, Rectangle bounds) {
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
		B2DFactory.makeBoxFixture(b2body, this, CFCAT_BITS, CFMASK_BITS, getBodySize().x, getBodySize().y);
	}
}
