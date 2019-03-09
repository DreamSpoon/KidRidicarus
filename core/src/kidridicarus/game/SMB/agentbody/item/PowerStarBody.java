package kidridicarus.game.SMB.agentbody.item;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agentbody.general.MobileAgentBody;
import kidridicarus.common.agentbody.sensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.SMB.agent.item.PowerStar;
import kidridicarus.game.SMB.agentbody.BumpableBody;

public class PowerStarBody extends MobileAgentBody implements BumpableBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private PowerStar parent;
	private SolidBoundSensor hmSensor;

	public PowerStarBody(PowerStar parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef;
		bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.gravityScale = 0.5f;	// floaty
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.restitution = 1f;	// bouncy
		// items contact mario but can pass through goombas, turtles, etc.
		hmSensor = new SolidBoundSensor(parent);
		B2DFactory.makeBoxFixture(b2body, fdef, hmSensor, CommonCF.SOLID_ITEM_CFCAT, CommonCF.SOLID_ITEM_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public void onBump(Agent bumpingAgent) {
		parent.onBump(bumpingAgent);
	}

	public boolean isMoveBlocked(boolean movingRight) {
		return hmSensor.isHMoveBlocked(getBounds(), movingRight);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
