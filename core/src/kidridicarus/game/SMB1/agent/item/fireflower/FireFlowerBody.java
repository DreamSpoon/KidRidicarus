package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agent.halfactor.HalfActorBody;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class FireFlowerBody extends HalfActorBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	protected SolidContactSpine spine;

	public FireFlowerBody(FireFlower parent, World world) {
		super(parent, world);
		spine = null;
	}

	public void finishSprout(Vector2 position) {
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT));
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// create new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()));
		spine = new SolidContactSpine(this);
		// solid main fixture
		B2DFactory.makeBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				spine.createSolidContactSensor(), bounds.width, bounds.height);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK,
				spine.createAgentSensor(), bounds.width, bounds.height);
	}

	@Override
	public PowerupBrainContactFrameInput processContactFrame() {
		if(spine == null)
			return null;
		return new PowerupBrainContactFrameInput(spine.getTouchingPowerupTaker());
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
