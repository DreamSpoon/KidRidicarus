package kidridicarus.game.Metroid.agentbody.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.Metroid.agent.item.MaruMari;

public class MaruMariBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(4f);
	private static final float BODY_HEIGHT = UInfo.P2M(4f);

	private MaruMari parent;

	public MaruMariBody(MaruMari parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		// items contact mario but can pass through goombas, turtles, etc.
		CFBitSeq catBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
		b2body.setGravityScale(0f);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
