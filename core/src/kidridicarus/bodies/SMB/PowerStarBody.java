package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.bodies.BotBumpableBody;
import kidridicarus.bodies.MobileRobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.item.PowerStar;
import kidridicarus.tools.B2DFactory;

public class PowerStarBody extends MobileRobotBody implements BotBumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private PowerStar role;

	public PowerStarBody(PowerStar role, World world, Vector2 position) {
		this.role = role;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef;
		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.gravityScale = 0.5f;	// floaty
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		// items touch mario but can pass through goombas, turtles, etc.
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOSENSOR_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		role.onBump(perp, fromCenter);
	}

	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
		role.onTouchBoundLine(seg);
	}

	@Override
	public RobotRole getParent() {
		return role;
	}
}
