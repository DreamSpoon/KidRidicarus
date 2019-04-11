package kidridicarus.game.agent.SMB1.player.mariofireball;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MarioFireballBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(5f);
	private static final float BODY_HEIGHT = UInfo.P2M(5f);
	private static final float AGENT_SENSOR_WIDTH = UInfo.P2M(8f);
	private static final float AGENT_SENSOR_HEIGHT = UInfo.P2M(8f);
	private static final float GRAVITY_SCALE = 2f;	// heavy

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.SOLID_BOUND_BIT, CommonCF.Alias.KEEP_ALIVE_BIT,
			CommonCF.Alias.ROOM_BIT);

	private MarioFireballSpine spine;
	private Vector2 prevVelocity;

	public MarioFireballBody(MarioFireball parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(position, velocity);
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position, velocity);
		createFixtures();
	}

	private void createBody(World world, Vector2 position, Vector2 velocity) {
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		b2body.setBullet(true);
		spine = new MarioFireballSpine(this);
		prevVelocity = velocity.cpy();
	}

	private void createFixtures() {
		createMainFixture();
		createAgentSensorFixture();
	}

	private void createMainFixture() {
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0f;		// slippery
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createSolidContactSensor(), MAIN_CFCAT, MAIN_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	private void createAgentSensorFixture() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(), AS_CFCAT, AS_CFMASK,
				AGENT_SENSOR_WIDTH, AGENT_SENSOR_HEIGHT);
	}

	public void postUpdate() {
		prevVelocity.set(b2body.getLinearVelocity());
	}

	public void setGravityScale(float scale) {
		b2body.setGravityScale(scale);
	}

	public MarioFireballSpine getSpine() {
		return spine;
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}
}
