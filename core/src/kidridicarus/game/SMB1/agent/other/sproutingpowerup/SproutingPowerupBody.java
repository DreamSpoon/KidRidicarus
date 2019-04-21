package kidridicarus.game.SMB1.agent.other.sproutingpowerup;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agentbody.MotileAgentBody;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public abstract class SproutingPowerupBody extends MotileAgentBody {
	protected SolidContactSpine spine;

	public SproutingPowerupBody(SproutingPowerup parent, World world, Rectangle bounds, Vector2 velocity) {
		super(parent, world);
		defineBody(bounds, velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// create new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		spine = new SolidContactSpine(this);
		// solid main fixture
		B2DFactory.makeBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				spine.createSolidContactSensor(), bounds.width, bounds.height);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK,
				spine.createAgentSensor(), bounds.width, bounds.height);
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
