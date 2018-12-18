package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.bodies.RobotBody;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.item.FireFlower;
import kidridicarus.tools.B2DFactory;

public class FireFlowerBody extends RobotBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private FireFlower role;

	public FireFlowerBody(FireFlower role, World world, Vector2 position) {
		this.role = role;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		// items touch mario but can pass through goombas, turtles, etc.
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.ITEM_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOSENSOR_BIT), position, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public RobotRole getParent() {
		return role;
	}
}
