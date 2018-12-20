package kidridicarus.agent.bodies.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.PipeWarp;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.info.GameInfo;

public class PipeWarpBody extends AgentBody {
	private PipeWarp parent;

	public PipeWarpBody(PipeWarp parent, World world, Rectangle bounds) {
		this.parent = parent;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
// TODO: fix mario sensor code
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.PIPE_BIT,
//				(short) (GameInfo.MARIOFOOT_BIT | GameInfo.MARIOSIDE_BIT | GameInfo.MARIOHEAD_BIT), bounds);
				(short) (GameInfo.GUIDEFOOT_BIT | GameInfo.GUIDEHEAD_BIT), bounds);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
