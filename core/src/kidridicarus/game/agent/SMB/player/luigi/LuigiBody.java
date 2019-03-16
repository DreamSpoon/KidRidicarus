package kidridicarus.game.agent.SMB.player.luigi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class LuigiBody extends AgentBody {
	private static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	private static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));
	private static final float FOOT_WIDTH = UInfo.P2M(5f);
	private static final float FOOT_HEIGHT = UInfo.P2M(2f);
	// TODO head size needs some work, bump tile is inconsistent
	private static final float HEAD_WIDTH = UInfo.P2M(10f);
	private static final float HEAD_HEIGHT = UInfo.P2M(12f);

	// main body
	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	// agent sensor
	private static final CFBitSeq AS_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.POWERUP_BIT, CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = CommonCF.NO_CONTACT_CFCAT;
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT,
			CommonCF.Alias.DESPAWN_BIT);
	// ground sensor
	private static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);
	// bumptile sensor
	private static final CFBitSeq BUMPTILE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq BUMPTILE_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);

	private static final float FRICTION = 0f;
	private static final float GRAVITY_SCALE = 2f;

	private Luigi parent;
	private World world;
	private LuigiSpine spine;
	private Vector2 prevPosition;
	private Vector2 prevVelocity;
	private Fixture acSensorFixture;

	public LuigiBody(Luigi parent, World world, Vector2 position, Vector2 velocity, boolean isBigBody,
			boolean isDucking) {
		this.parent = parent;
		this.world = world;
		defineBody(position, velocity, isBigBody, isDucking);
	}

	public void defineBody(Vector2 position, Vector2 velocity, boolean isBigBody, boolean isDucking) {
		if(isBigBody && !isDucking)
			setBodySize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBodySize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);

		createBody(position, velocity);
		createFixtures();
	}

	private void createBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		prevPosition = position.cpy();
		prevVelocity = velocity.cpy();

		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, this,
				MAIN_CFCAT, MAIN_CFMASK, getBodySize().x, getBodySize().y);

		spine = new LuigiSpine(this);
	}

	private void createFixtures() {
		// create agent contact sensor fixture
		acSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				AS_ENABLED_CFCAT, AS_ENABLED_CFMASK, getBodySize().x, getBodySize().y);
		// create ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createOnGroundSensor(),
				GROUND_SENSOR_CFCAT, GROUND_SENSOR_CFMASK,
				FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBodySize().y/2f));
		// head sensor for detecting head hits against bumptiles
		B2DFactory.makeSensorBoxFixture(b2body, spine.createBumpTileSensor(),
				BUMPTILE_SENSOR_CFCAT, BUMPTILE_SENSOR_CFMASK,
				HEAD_WIDTH, HEAD_HEIGHT, new Vector2(0f, getBodySize().y/2f));
	}

	public void postUpdate() {
		prevPosition.set(b2body.getPosition());
		prevVelocity.set(b2body.getLinearVelocity());
	}

	public void applyBodyImpulse (Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}

	public void allowOnlyDeadContacts() {
		// disable all, and...
		disableAllContacts();
		// ... re-enable the needed agent contact sensor bits
		((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
		((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		acSensorFixture.refilter();
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}

	public LuigiSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
