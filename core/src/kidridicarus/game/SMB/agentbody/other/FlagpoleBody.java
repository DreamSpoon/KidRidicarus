package kidridicarus.game.SMB.agentbody.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.SMB.agent.other.Flagpole;

public class FlagpoleBody extends AgentBody {
	private Flagpole parent;

	public FlagpoleBody(Flagpole parent, World world, Rectangle bounds) {
		this.parent = parent;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		CFBitSeq catBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, catBits, maskBits, bounds);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
