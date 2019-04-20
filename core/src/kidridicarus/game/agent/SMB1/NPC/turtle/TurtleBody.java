package kidridicarus.game.agent.SMB1.NPC.turtle;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class TurtleBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = UInfo.P2M(12f);
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.ROOM_BIT);
	private static final CFBitSeq AS_DISABLED_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_DISABLED_CFMASK = new CFBitSeq(CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.ROOM_BIT);

	private TurtleSpine spine;
	private Fixture acSensorFixture;

	public TurtleBody(Turtle parent, World world, Vector2 position, Vector2 velocity) {
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
		spine = new TurtleSpine(this);
		createFixtures();
	}

	private void createFixtures() {
		SolidContactSensor solidSensor = spine.createSolidContactSensor();
		// create main fixture
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, solidSensor, getBounds().width, getBounds().height);
		// create agent sensor fixture
		AgentContactHoldSensor sensor = spine.createAgentSensor();
		sensor.chainTo(spine.createHeadBounceSensor());
		acSensorFixture = B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, sensor,
				getBounds().width, getBounds().height);
		// create ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, solidSensor,
				FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -getBounds().height/2f));
	}

	public void allowOnlyDeadBumpContacts() {
		disableAllContacts();
		// change the needed agent contact sensor bits
		((AgentBodyFilter) acSensorFixture.getUserData()).categoryBits = AS_DISABLED_CFCAT;
		((AgentBodyFilter) acSensorFixture.getUserData()).maskBits = AS_DISABLED_CFMASK;
		acSensorFixture.refilter();
	}

	public TurtleSpine getSpine() {
		return spine;
	}
}
