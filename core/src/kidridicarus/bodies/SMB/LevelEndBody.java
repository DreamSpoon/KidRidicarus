package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.bodies.RobotBody;
import kidridicarus.info.GameInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.LevelEndTrigger;
import kidridicarus.tools.B2DFactory;

public class LevelEndBody extends RobotBody {
	private LevelEndTrigger role;

	public LevelEndBody(LevelEndTrigger role, World world, Rectangle bounds) {
		this.role = role;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.ROBOT_BIT,
				GameInfo.MARIO_ROBOSENSOR_BIT, bounds);
	}

	@Override
	public RobotRole getParent() {
		return role;
	}
}
