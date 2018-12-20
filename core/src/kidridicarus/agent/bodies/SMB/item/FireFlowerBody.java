package kidridicarus.agent.bodies.SMB.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.item.FireFlower;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.info.GameInfo;
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
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.ITEM_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.GUIDE_AGENTSENSOR_BIT), position, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
