package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.GameInfo;
import kidridicarus.bodies.B2DFactory;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.item.StaticCoin;

public class StaticCoinBody extends RobotBody {
	private static final float BODY_WIDTH = GameInfo.P2M(16f);
	private static final float BODY_HEIGHT = GameInfo.P2M(16f);

	private StaticCoin role;

	public StaticCoinBody(StaticCoin role, World world, Vector2 position) {
		this.role = role;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef;
		bdef = new BodyDef();
		bdef.position.set(position.x, position.y);
		bdef.type = BodyDef.BodyType.StaticBody;
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		fdef.filter.categoryBits = GameInfo.ITEM_BIT;
		fdef.filter.maskBits = GameInfo.MARIO_ROBOSENSOR_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public RobotRole getRole() {
		return role;
	}

	// coin doesnt move, nothing to do
	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
	}
}
