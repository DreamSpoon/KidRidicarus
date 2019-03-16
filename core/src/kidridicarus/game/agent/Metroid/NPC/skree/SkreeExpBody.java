package kidridicarus.game.agent.Metroid.NPC.skree;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SkreeExpBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(6);
	private static final float BODY_HEIGHT = UInfo.P2M(6);

	private SkreeExp parent;

	public SkreeExpBody(SkreeExp parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 0f;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		B2DFactory.makeBoxFixture(b2body, fdef, this, CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
