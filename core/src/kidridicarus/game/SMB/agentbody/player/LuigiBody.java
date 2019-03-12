package kidridicarus.game.SMB.agentbody.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.general.MobileAgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.SMB.agent.player.Luigi;

public class LuigiBody extends MobileAgentBody {
	private static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	private static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));

	// main body
	private static final CFBitSeq MAINBODY_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAINBODY_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final float FRICTION = 0f;

	private Luigi parent;
	private World world;
	private LuigiSpine spine;

	public LuigiBody(Luigi parent, World world, Vector2 position, boolean isBigBody) {
		this.world = world;
		defineBody(position, isBigBody);
	}

	private void defineBody(Vector2 position, boolean isBigBody) {
		if(isBigBody)
			setBodySize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBodySize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);

		createBody(position);
		createSpine();
	}

	private void createBody(Vector2 position) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, this, MAINBODY_CFCAT, MAINBODY_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	private void createSpine() {
		spine = new LuigiSpine(this, b2body);
		spine.createAgentSensor();
		spine.createGroundSensor();
	}

	public void applyBodyImpulse (Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}

	public LuigiSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
