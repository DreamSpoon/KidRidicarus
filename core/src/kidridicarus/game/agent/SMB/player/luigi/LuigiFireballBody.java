package kidridicarus.game.agent.SMB.player.luigi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class LuigiFireballBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);
	private static final float GRAVITY_SCALE = 2f;

	private static final CFBitSeq MAIN_ENABLED_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_ENABLED_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq MAIN_DISABLED_CFCAT = CommonCF.NO_CONTACT_CFCAT;
	private static final CFBitSeq MAIN_DISABLED_CFMASK = CommonCF.NO_CONTACT_CFMASK;

	private static final CFBitSeq AS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.DESPAWN_BIT);

	private LuigiFireball parent;
	private LuigiFireballSpine spine;
	private Fixture mainBodyFixture;
	private Fixture acSensorFixture;

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
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, fdef, spine.createHMSensor(),
				MAIN_ENABLED_CFCAT, MAIN_ENABLED_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensorFixture() {
		acSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentContactSensor(),
				AS_ENABLED_CFCAT, AS_ENABLED_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public void setMainSolid(boolean enabled) {
		if(enabled) {
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = MAIN_ENABLED_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = MAIN_ENABLED_CFMASK;
		}
		else {
			((AgentBodyFilter) mainBodyFixture.getUserData()).categoryBits = MAIN_DISABLED_CFCAT;
			((AgentBodyFilter) mainBodyFixture.getUserData()).maskBits = MAIN_DISABLED_CFMASK;
		}
		mainBodyFixture.refilter();
	}

	public void setAgentSensorEnabled(boolean enabled) {
		if(enabled) {
			((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_ENABLED_CFCAT;
			((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_ENABLED_CFMASK;
		}
		else {
			((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
			((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		}
		acSensorFixture.refilter();
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
