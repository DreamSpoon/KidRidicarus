package kidridicarus.game.agent.SMB1.item.powerstar;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.SMB_NPC_Spine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class PowerStarBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0.5f;

	private SMB_NPC_Spine spine;

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
		spine = new SMB_NPC_Spine(this);
		// agent and horiztonal move sensor fixture
		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		AgentContactHoldSensor sensor = spine.createAgentSensor();
		sensor.chainTo(spine.createHorizontalMoveSensor());
		B2DFactory.makeBoxFixture(b2body, fdef, sensor,
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public SMB_NPC_Spine getSpine() {
		return spine;
	}
}
