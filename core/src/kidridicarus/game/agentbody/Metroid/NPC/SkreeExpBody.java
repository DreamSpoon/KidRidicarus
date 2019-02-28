package kidridicarus.game.agentbody.Metroid.NPC;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.agent.Metroid.NPC.SkreeExp;

public class SkreeExpBody extends MobileAgentBody {
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
		FixtureDef fdef = new FixtureDef();
		CFBitSeq catBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CommonInfo.CFBits.AGENT_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, BODY_WIDTH, BODY_HEIGHT);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
