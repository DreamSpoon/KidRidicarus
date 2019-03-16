package kidridicarus.game.agent.SMB.player.luigi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class LuigiFireballBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);
	private static final float GRAVITY_SCALE = 2f;

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT);

	private LuigiFireball parent;
	private LuigiFireballSpine spine;

	public LuigiFireballBody(LuigiFireball parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position, velocity);
		createFixtures();
	}

	private void createBody(World world, Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);	// heavy

		spine = new LuigiFireballSpine(this);
	}

	private void createFixtures() {
		createMainFixture();
		createAgentSensorFixture();
	}

	private void createMainFixture() {
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0f;		// slippery
		fdef.restitution = 1f;	// bouncy
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createHMSensor(), MAIN_CFCAT, MAIN_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensorFixture() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentContactSensor(), AS_CFCAT, AS_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	public void setGravityScale(float scale) {
		b2body.setGravityScale(scale);
	}

	public LuigiFireballSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
