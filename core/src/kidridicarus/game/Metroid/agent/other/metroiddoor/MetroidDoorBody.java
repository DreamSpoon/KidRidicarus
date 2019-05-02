package kidridicarus.game.Metroid.agent.other.metroiddoor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MetroidDoorBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(8f);
	private static final float BODY_HEIGHT = UInfo.P2M(48f);

	private static final CFBitSeq MAIN_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.SOLID_BOUND_BIT);
	private static final CFBitSeq MAIN_ENABLED_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq MAIN_DISABLED_CFCAT = CommonCF.NO_CONTACT_CFCAT;
	private static final CFBitSeq MAIN_DISABLED_CFMASK = CommonCF.NO_CONTACT_CFMASK;

	private Fixture mainBodyFixture;

	public MetroidDoorBody(MetroidDoor parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(new Rectangle(position.x, position.y, 0f, 0f));
	}

	// velocity is ignored
	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()));
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, MAIN_ENABLED_CFCAT, MAIN_ENABLED_CFMASK, this,
				getBounds().width, getBounds().height);
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
}
