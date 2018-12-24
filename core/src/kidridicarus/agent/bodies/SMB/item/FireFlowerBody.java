package kidridicarus.agent.bodies.SMB.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contacts.CFBitSeq;
import kidridicarus.agency.contacts.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.item.FireFlower;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.info.UInfo;

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
		CFBitSeq catBits = new CFBitSeq(CFBit.SOLID_BIT, CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT, CFBit.GUIDE_SENSOR_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
