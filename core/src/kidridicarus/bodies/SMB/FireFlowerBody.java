package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.GameInfo;
import kidridicarus.bodies.B2DFactory;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.item.FireFlower;

public class FireFlowerBody extends RobotBody {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(12f);

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
	public RobotRole getRole() {
		return role;
	}

	// fire flower doesn't move, so this method is redundant
	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
	}
}
