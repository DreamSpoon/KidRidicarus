package kidridicarus.agent.body.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agency.helper.B2DFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.WarpPipe;
import kidridicarus.agent.body.AgentBody;

public class WarpPipeBody extends AgentBody {
	private WarpPipe parent;

	public WarpPipeBody(WarpPipe parent, World world, Rectangle bounds) {
		this.parent = parent;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		CFBitSeq catBits = new CFBitSeq(CFBit.PIPE_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, catBits, maskBits, bounds);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
