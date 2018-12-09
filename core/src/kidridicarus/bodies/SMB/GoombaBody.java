package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.GameInfo;
import kidridicarus.bodies.B2DFactory;
import kidridicarus.bodies.BotBumpableBody;
import kidridicarus.bodies.BotGroundCheckBody;
import kidridicarus.bodies.BotTouchBotBody;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.roles.PlayerRole;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.enemy.GoombaRole;

public class GoombaBody extends RobotBody implements BotGroundCheckBody, BotTouchBotBody, BotBumpableBody {
	private static final float BODY_WIDTH = GameInfo.P2M(14f);
	private static final float BODY_HEIGHT = GameInfo.P2M(14f);
	private static final float FOOT_WIDTH = GameInfo.P2M(12f);
	private static final float FOOT_HEIGHT = GameInfo.P2M(4f);

	private GoombaRole role;

	public GoombaBody(GoombaRole role, World world, Vector2 position, Vector2 velocity) {
		this.role = role;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeBoxBody(world, BodyType.DynamicBody, this, GameInfo.ROBOT_BIT,
				(short) (GameInfo.BOUNDARY_BIT | GameInfo.ROBOT_BIT | GameInfo.MARIO_ROBOSENSOR_BIT), position,
				BODY_WIDTH, BODY_HEIGHT);
		// start in the inactive state, becoming active when the player is close enough
		b2body.setActive(false);
		createBottomSensorFixture();
	}

	// create the foot sensor for detecting onGround
	private void createBottomSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footSensor;
		footSensor = new PolygonShape();
		footSensor.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.filter.categoryBits = GameInfo.ROBOTFOOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT;
		fdef.shape = footSensor;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(this);
	}

	@Override
	public void onTouchVertBoundLine(LineSeg seg) {
		role.onTouchBoundLine(seg);
	}

	@Override
	public void onBump(PlayerRole perp, Vector2 fromCenter) {
		role.onBump(perp, fromCenter);
	}

	@Override
	public void onTouchRobot(RobotBody robotBody) {
		role.onTouchRobot(robotBody.getRole());
	}

	@Override
	public RobotRole getRole() {
		return role;
	}
}
