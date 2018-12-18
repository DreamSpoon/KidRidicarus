package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.bodies.BotBumpableBody;
import kidridicarus.bodies.RobotBody;
import kidridicarus.info.GameInfo;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.BumpTile;
import kidridicarus.tools.B2DFactory;

public class BumpTileBody extends RobotBody implements BotBumpableBody {
	private BumpTile role;

	public BumpTileBody(World world, BumpTile role, Rectangle bounds) {
		this.role = role;
		setBodySize(bounds.width, bounds.height);

		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.StaticBody;
		bdef.position.set(bounds.getCenter(new Vector2()));
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		fdef.filter.categoryBits = GameInfo.BANGABLE_BIT;
		fdef.filter.maskBits = GameInfo.MARIOHEAD_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, bounds.width, bounds.height);
	}

	@Override
	public RobotRole getParent() {
		return role;
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		role.onBump(perp, fromCenter);
	}
}
