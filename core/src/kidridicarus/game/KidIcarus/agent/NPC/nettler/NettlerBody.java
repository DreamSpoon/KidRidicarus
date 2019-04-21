package kidridicarus.game.KidIcarus.agent.NPC.nettler;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.agentbody.MotileAgentBody;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class NettlerBody extends MotileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(6f);
	private static final float BODY_HEIGHT = UInfo.P2M(14f);
	private static final float FOOT_WIDTH = BODY_WIDTH;
	private static final float FOOT_HEIGHT = UInfo.P2M(4f);
	private static final float PLAYER_SENSOR_WIDTH = UInfo.P2M(256);
	private static final float PLAYER_SENSOR_HEIGHT = UInfo.P2M(256);
	private static final Vector2 PLAYER_SENSOR_OFFSET = UInfo.VectorP2M(0f, -80);

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.ROOM_BIT);

	protected class NettlerBodyContactFrameOutput {
		public List<ContactDmgTakeAgent> contactDmgTakeAgents;
		public NettlerBodyContactFrameOutput(List<ContactDmgTakeAgent> contactDmgTakeAgents) {
			this.contactDmgTakeAgents = contactDmgTakeAgents;
		}
	}

	protected class NettlerBodyFrameOutput {
		public float timeDelta;
		public RoomBox roomBox;
		public boolean isContactKeepAlive;
		public boolean isContactDespawn;
		public NettlerBodyFrameOutput(float timeDelta, RoomBox roomBox, boolean isContactKeepAlive,
				boolean isContactDespawn) {
			this.timeDelta = timeDelta;
			this.roomBox = roomBox;
			this.isContactKeepAlive = isContactKeepAlive;
			this.isContactDespawn = isContactDespawn;
		}
	}

	private NettlerSpine spine;

	public NettlerBody(Nettler parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		spine = new NettlerSpine(this);
		createFixtures();
	}

	private void createFixtures() {
		// main body fixture
		SolidContactSensor solidSensor = spine.createSolidContactSensor();
		B2DFactory.makeBoxFixture(b2body, MAIN_CFCAT, MAIN_CFMASK, solidSensor, getBounds().width,
				getBounds().height);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				getBounds().width, getBounds().height);
		// ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				solidSensor, FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBounds().height/2f));
		// create player sensor fixture that covers most of the screen and detects players to target
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK,
				spine.createPlayerSensor(), PLAYER_SENSOR_WIDTH, PLAYER_SENSOR_HEIGHT, PLAYER_SENSOR_OFFSET);
	}

	public NettlerBodyContactFrameOutput processContactFrame() {
		return new NettlerBodyContactFrameOutput(spine.getContactDmgTakeAgents());
	}

	public NettlerBodyFrameOutput processFrame(float delta) {
		return new NettlerBodyFrameOutput(delta, spine.getCurrentRoom(), spine.isTouchingKeepAlive(),
				spine.isContactDespawn());
	}

	public NettlerSpine getSpine() {
		return spine;
	}
}
