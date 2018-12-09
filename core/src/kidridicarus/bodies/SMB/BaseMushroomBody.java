package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.GameInfo;
import kidridicarus.bodies.B2DFactory;
import kidridicarus.bodies.BotBumpableBody;
import kidridicarus.bodies.BotGroundCheckBody;
import kidridicarus.bodies.BotTouchBotBody;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.item.BaseMushroom;

public class BaseMushroomBody extends RobotBody implements BotGroundCheckBody, BotTouchBotBody, BotBumpableBody {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(12f);
	private static final float FOOT_WIDTH = GameInfo.P2M(12f);
	private static final float FOOT_HEIGHT = GameInfo.P2M(4f);

	private BaseMushroom role;

	public BaseMushroomBody(BaseMushroom role, World world, Vector2 position) {
		this.role = role;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, GameInfo.ITEM_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.MARIO_ROBOSENSOR_BIT), position, BODY_WIDTH, BODY_HEIGHT);
		createBottomSensor();
	}

	private void createBottomSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footSensor = new PolygonShape();
		footSensor.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = footSensor;
		fdef.isSensor = true;
		fdef.filter.categoryBits = GameInfo.ROBOTFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		b2body.createFixture(fdef).setUserData(this);
	}

	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
		role.onTouchVertBoundLine(seg);
	}
	
	@Override
	public void onTouchRobot(RobotBody robo) {
		role.onTouchRobot(robo.getRole());
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		role.onBump(perp, fromCenter);
	}

	@Override
	public RobotRole getRole() {
		return role;
	}
}
