package kidridicarus.game.agent.KidIcarus.player.pitarrow;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentspine.SolidContactSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.Direction4;

public class PitArrowBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(3);
	private static final float BODY_HEIGHT = UInfo.P2M(3);

	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.ROOM_BIT);

	private Direction4 arrowDir;
	private SolidContactSpine spine;

	public PitArrowBody(PitArrow parent, World world, Vector2 position, Vector2 velocity, Direction4 arrowDir) {
		super(parent, world);
		this.arrowDir = arrowDir;
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		// if horizontal then set body size like normal...
		if(arrowDir.isHorizontal())
			setBodySize(BODY_WIDTH, BODY_HEIGHT);
		// but if vertical then rotate body size by 90 degrees
		else
			setBodySize(BODY_HEIGHT, BODY_WIDTH);

		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		b2body.setBullet(true);

		spine = new SolidContactSpine(this);

		// create main fixture
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, spine.createSolidContactSensor(),
				getBodySize().x, getBodySize().y);
		// create agent contact sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				getBodySize().x, getBodySize().y);
	}

	public SolidContactSpine getSpine() {
		return spine;
	}
}
