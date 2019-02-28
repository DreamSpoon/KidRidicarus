package kidridicarus.game.agent.body.SMB.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.AgentBody;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.game.agent.SMB.item.FireFlower;
import kidridicarus.game.info.GameInfo;

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
		CFBitSeq catBits = new CFBitSeq(GameInfo.CFBits.ITEM_BIT);
		CFBitSeq maskBits = new CFBitSeq(GameInfo.CFBits.AGENT_BIT, GameInfo.CFBits.SOLID_BOUND_BIT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, catBits, maskBits, position,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
