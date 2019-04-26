package kidridicarus.game.SMB1.agent.item.mushroom;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agent.fullactor.FullActorBody;
import kidridicarus.common.agentbrain.BrainFrameInput;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentbrain.RoomingBrainFrameInput;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class BaseMushroomBody extends FullActorBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	protected SolidContactSpine spine;

	public BaseMushroomBody(BaseMushroom parent, World world) {
		super(parent, world);
		spine = null;
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		spine = new SolidContactSpine(this);

		// create main fixture
		SolidContactSensor solidSensor = spine.createSolidContactSensor();
		B2DFactory.makeBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, solidSensor,
				bounds.width, bounds.height);
		// create on ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, solidSensor,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -bounds.height/2f));
		// create agent sensor
		AgentContactHoldSensor agentSensor = spine.createAgentSensor();
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, agentSensor,
				bounds.width, bounds.height);
	}

	public void finishSprout(Vector2 position) {
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT));
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
