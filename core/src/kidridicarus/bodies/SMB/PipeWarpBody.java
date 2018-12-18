package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.bodies.RobotBody;
import kidridicarus.info.GameInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.PipeWarp;
import kidridicarus.tools.B2DFactory;

public class PipeWarpBody extends RobotBody {
	private PipeWarp role;

	public PipeWarpBody(PipeWarp role, World world, Rectangle bounds) {
		this.role = role;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
// TODO: fix mario sensor code
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, GameInfo.PIPE_BIT,
//				(short) (GameInfo.MARIOFOOT_BIT | GameInfo.MARIOSIDE_BIT | GameInfo.MARIOHEAD_BIT), bounds);
				(short) (GameInfo.MARIOFOOT_BIT | GameInfo.MARIOHEAD_BIT), bounds);
	}

	@Override
	public RobotRole getParent() {
		return role;
	}
}
