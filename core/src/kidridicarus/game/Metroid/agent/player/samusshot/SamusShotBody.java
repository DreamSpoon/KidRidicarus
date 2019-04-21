package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MotileAgentBody;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class SamusShotBody extends MotileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(1);
	private static final float BODY_HEIGHT = UInfo.P2M(1);
	private static final float SENSOR_WIDTH = UInfo.P2M(3);
	private static final float SENSOR_HEIGHT = UInfo.P2M(3);

	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.ROOM_BIT);

	private SolidContactSpine spine;

	public SamusShotBody(SamusShot parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		b2body.setBullet(true);

		spine = new SolidContactSpine(this);

		// create main fixture
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, spine.createSolidContactSensor(),
				bounds.width, bounds.height);
		// create agent contact sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				SENSOR_WIDTH, SENSOR_HEIGHT);
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
