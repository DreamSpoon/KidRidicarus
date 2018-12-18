package kidridicarus.bodies.SMB;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.bodies.BotTouchBotBody;
import kidridicarus.bodies.MobileRobotBody;
import kidridicarus.bodies.RobotBody;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo;
import kidridicarus.info.UInfo;
import kidridicarus.roles.RobotRole;
import kidridicarus.roles.robot.SMB.MarioFireball;
import kidridicarus.tools.B2DFactory;

public class MarioFireballBody extends MobileRobotBody implements BotTouchBotBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);

	private MarioFireball role;

	public MarioFireballBody(MarioFireball role, World world, Vector2 position, Vector2 velocity) {
		this.role = role;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 2f;	// heavy
		bdef.type = BodyDef.BodyType.DynamicBody;
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0f;		// slippery
		fdef.restitution = 1f;	// bouncy
		fdef.filter.categoryBits = GameInfo.ROBOT_BIT;
		fdef.filter.maskBits = GameInfo.BOUNDARY_BIT | GameInfo.ROBOT_BIT;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public RobotRole getParent() {
		return role;
	}

	@Override
	protected void onTouchVertBoundLine(LineSeg seg) {
		role.onTouchBoundLine(seg);
	}

	@Override
	public void onTouchRobot(RobotBody robo) {
		role.onTouchRobot(robo.getParent());
	}

	public void setGravityScale(float scale) {
		b2body.setGravityScale(scale);
	}
}
