package kidridicarus.common.agent.semisolidfloor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
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

	// Velocity is ignored. TODO mouse joint needed to implement velocity (properly).
	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		AgentBodyFilter abf = new AgentBodyFilter(CFCAT_BITS, CFMASK_BITS, this);
		abf.preSolver = new SemiSolidPreSolver(abf);
		B2DFactory.makeBoxFixture(b2body, abf, getBounds().width, getBounds().height);
	}
}
