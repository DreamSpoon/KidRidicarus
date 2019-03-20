package kidridicarus.game.agent.Metroid.other.metroiddoor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MetroidDoorBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(8f);
	private static final float BODY_HEIGHT = UInfo.P2M(48f);

	private static final CFBitSeq MAIN_ENABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.SOLID_BOUND_BIT);
	private static final CFBitSeq MAIN_ENABLED_CFMASK = new CFBitSeq(true);
	private static final CFBitSeq MAIN_DISABLED_CFCAT = CommonCF.NO_CONTACT_CFCAT;
	private static final CFBitSeq MAIN_DISABLED_CFMASK = CommonCF.NO_CONTACT_CFMASK;

	private Fixture mainBodyFixture;

	public MetroidDoorBody(MetroidDoor parent, World world, Vector2 position) {
		super(parent);
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		// TODO: verify that catBits should be SOLID_BOUND_BIT, e.g.
		//   -will this interfere with on ground detection?
		//   -will zoomer be able to walk on door?
		//   -will this agent be confused with a solid bound line seg from collision map?
		b2body = B2DFactory.makeStaticBody(world, position);
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, this,
				MAIN_ENABLED_CFCAT, MAIN_ENABLED_CFMASK, BODY_WIDTH, BODY_HEIGHT);
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
