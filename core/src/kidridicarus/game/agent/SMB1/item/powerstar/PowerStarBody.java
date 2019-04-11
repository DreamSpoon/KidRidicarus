package kidridicarus.game.agent.SMB1.item.powerstar;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class PowerStarBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0.5f;

	private SolidContactSpine spine;

	public PowerStarBody(PowerStar parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(position, velocity);
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);

		spine = new SolidContactSpine(this);

		// create main fixture
		// create agent sensor fixture
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createSolidContactSensor(),
				CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, getBodySize().x, getBodySize().y);
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, getBodySize().x, getBodySize().y);
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
