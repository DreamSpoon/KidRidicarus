package kidridicarus.game.SMB1.agent.item.powerstar;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agent.fullactor.FullActorBody;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class PowerStarBody extends FullActorBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0.5f;

	protected SolidContactSpine spine;

	public PowerStarBody(PowerStar parent, World world) {
		super(parent, world);
		spine = null;
	}

	public void finishSprout(Vector2 position, Vector2 velocity) {
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new SolidContactSpine(this);

		// create main fixture
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		B2DFactory.makeBoxFixture(b2body, fdef, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				spine.createSolidContactSensor(), bounds.width, bounds.height);
		// create agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK,
				spine.createAgentSensor(), bounds.width, bounds.height);
	}

	@Override
	public PowerupBrainContactFrameInput processContactFrame() {
		if(spine == null)
			return null;
		return new PowerupBrainContactFrameInput(spine.getTouchingPowerupTaker());
	}

	@Override
	public BrainFrameInput processFrame(float delta) {
		if(spine == null)
			return new BrainFrameInput(delta);
		return new RoomingBrainFrameInput(delta, spine.getCurrentRoom(), spine.isTouchingKeepAlive(),
				spine.isContactDespawn());
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
