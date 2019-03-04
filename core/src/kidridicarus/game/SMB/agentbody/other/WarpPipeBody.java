package kidridicarus.game.SMB.agentbody.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.agent.general.PipeWarp;
import kidridicarus.common.info.CommonCF;

public class WarpPipeBody extends AgentBody {
	private PipeWarp parent;

	public WarpPipeBody(PipeWarp parent, World world, Rectangle bounds) {
		this.parent = parent;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, CommonCF.WARPPIPE_CFCAT,
				CommonCF.WARPPIPE_CFMASK, bounds);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
