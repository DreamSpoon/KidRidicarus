package kidridicarus.game.KidIcarus.agent.item.angelheart;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentbrain.PowerupBrainContactFrameInput;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class AngelHeartBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(3f);
	private static final float BODY_HEIGHT = UInfo.P2M(3f);

	private BasicAgentSpine spine;

	public AngelHeartBody(AngelHeart parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT));
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// define new body
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(0f);
		spine = new BasicAgentSpine(this);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK,
				spine.createAgentSensor(), getBounds().width, getBounds().height);
	}

	public PowerupBrainContactFrameInput processContactFrame() {
		return new PowerupBrainContactFrameInput(spine.getCurrentRoom(), spine.isTouchingKeepAlive(),
				spine.isContactDespawn(), spine.getTouchingPowerupTaker());
	}

	public BasicAgentSpine getSpine() {
		return spine;
	}
}
