package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.GameInfo;
import kidridicarus.bodies.B2DFactory;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.PipeWarp;

public class PipeWarpBody extends RobotBody {
	private PipeWarp role;

	public PipeWarpBody(PipeWarp role, World world, Rectangle bounds) {
		this.role = role;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.PIPE_BIT,
				(short) (GameInfo.MARIOFOOT_BIT | GameInfo.MARIOSIDE_BIT | GameInfo.MARIOHEAD_BIT), bounds);
	}

	@Override
	public RobotRole getRole() {
		return role;
	}

	// redundant
	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
	}
}
