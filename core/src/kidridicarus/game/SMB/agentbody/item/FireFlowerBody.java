package kidridicarus.game.SMB.agentbody.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.SMB.agent.item.FireFlower;

public class FireFlowerBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private FireFlower parent;

	public FireFlowerBody(FireFlower parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		// items contact mario but can pass through goombas, turtles, etc.
		CFBitSeq catBits = new CFBitSeq(CommonInfo.CFBits.ITEM_BIT);
		CFBitSeq maskBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT, CommonInfo.CFBits.SOLID_BOUND_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
