package kidridicarus.game.agent.KidIcarus.NPC.specknose;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.agentspine.KidIcarus.FlyBallSpine;

public class SpecknoseBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0f;

	private static final int ACCEL_X_LEFT = 4;
	private static final int ACCEL_X_RIGHT = 13;
	private static final int ACCEL_Y_BOTTOM = -12;
	private static final int ACCEL_Y_TOP = -2;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.SPAWNTRIGGER_BIT);

	private FlyBallSpine spine;

	public SpecknoseBody(Specknose parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				new Vector2(0f, 0f));
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new FlyBallSpine(this, new Rectangle(ACCEL_X_LEFT, ACCEL_Y_BOTTOM,
				ACCEL_X_RIGHT-ACCEL_X_LEFT, ACCEL_Y_TOP-ACCEL_Y_BOTTOM));
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				getBounds().width, getBounds().height);
	}

	public FlyBallSpine getSpine() {
		return spine;
	}
}
