package kidridicarus.game.agent.SMB1.item.powerstar;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.agent.SMB1.other.sproutingpowerup.SproutingPowerupBody;

public class PowerStarBody extends SproutingPowerupBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0.5f;

	public PowerStarBody(PowerStar parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world, new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f,
				BODY_WIDTH, BODY_HEIGHT), velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBodySize(bounds.getWidth(), bounds.getHeight());
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new SolidContactSpine(this);

		// create main fixture
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createSolidContactSensor(),
				CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, getBodySize().x, getBodySize().y);
		// create agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, getBodySize().x, getBodySize().y);
	}
}
