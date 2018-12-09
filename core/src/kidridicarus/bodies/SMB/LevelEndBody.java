package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.GameInfo;
import kidridicarus.bodies.B2DFactory;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.LevelEnd;

public class LevelEndBody extends RobotBody {
	private LevelEnd role;

	public LevelEndBody(LevelEnd role, World world, Rectangle bounds) {
		this.role = role;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		// ROBOT_BIT needed so mario's fireballs explode upon hitting flagpole
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.ROBOT_BIT,
				GameInfo.MARIO_ROBOSENSOR_BIT, bounds);
	}

	@Override
	public RobotRole getRole() {
		return role;
	}

	// redundant
	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
	}

	@Override
	public void setActive(boolean active) {
		b2body.setActive(active);
	}
}
