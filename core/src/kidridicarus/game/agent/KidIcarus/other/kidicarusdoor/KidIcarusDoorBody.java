package kidridicarus.game.agent.KidIcarus.other.kidicarusdoor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class KidIcarusDoorBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(27f);
	private static final Vector2 BODY_OFFSET = UInfo.VectorP2M(0f, -2.5f);
	private static final float ROOF_WIDTH = UInfo.P2M(13f);
	private static final float ROOF_HEIGHT = UInfo.P2M(4f);

	private static final CFBitSeq CLOSED_CFCAT = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);
	private static final CFBitSeq CLOSED_CFMASK = new CFBitSeq(true);
	private static final CFBitSeq OPENED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq OPENED_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq ROOF_CFCAT = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);
	private static final CFBitSeq ROOF_CFMASK = new CFBitSeq(true);

	private BasicAgentSpine spine;
	private AgentContactHoldSensor agentSensor;
	private Fixture mainBodyFixture;
	private boolean isOpened;

	public KidIcarusDoorBody(KidIcarusDoor parent, World world, Vector2 position, boolean isOpened) {
		super(parent, world);
		this.isOpened = isOpened;
		defineBody(new Rectangle(position.x, position.y, 0f, 0f));
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeStaticBody(world, bounds.getCenter(new Vector2()).add(BODY_OFFSET));
		spine = new BasicAgentSpine(this);
		// create the agent sensor, it will be used now and/or later
		agentSensor = spine.createAgentSensor();
		mainBodyFixture = B2DFactory.makeBoxFixture(b2body, isOpened ? OPENED_CFCAT : CLOSED_CFCAT,
				isOpened ? OPENED_CFMASK : CLOSED_CFMASK, agentSensor, getBodySize().x, getBodySize().y);
		// solid roof that player can stand on
		B2DFactory.makeBoxFixture(b2body, ROOF_CFCAT, ROOF_CFMASK, this, ROOF_WIDTH, ROOF_HEIGHT,
				new Vector2(0f, (BODY_HEIGHT+ROOF_HEIGHT)/2f));
	}

	public void setOpened(boolean isOpened) {
		if(this.isOpened != isOpened) {
			this.isOpened = isOpened;
			mainBodyFixture.setUserData(new AgentBodyFilter(isOpened ? OPENED_CFCAT : CLOSED_CFCAT,
					isOpened ? OPENED_CFMASK : CLOSED_CFMASK, agentSensor));
			mainBodyFixture.refilter();
		}
	}

	public BasicAgentSpine getSpine() {
		return spine;
	}
}
