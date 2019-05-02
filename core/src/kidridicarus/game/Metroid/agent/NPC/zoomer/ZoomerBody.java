package kidridicarus.game.Metroid.agent.NPC.zoomer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.agentsensor.SolidContactSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.DiagonalDir4;

public class ZoomerBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float SENSOR_SIZEFACTOR = 1.2f;
	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.ROOM_BIT);

	private ZoomerSpine spine;
	private Vector2 prevPosition;

	public ZoomerBody(Zoomer parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(bounds.getWidth(), bounds.getHeight());
		prevPosition = bounds.getCenter(new Vector2());
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new ZoomerSpine(this);
		// main fixture
		B2DFactory.makeBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, this,
				getBounds().width, getBounds().height);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				getBounds().width, getBounds().height);
		// crawl sensor fixtures
		createCrawlSensorFixtures();
	}

	private void createCrawlSensorFixtures() {
		Vector2 subBoxSize = new Vector2(BODY_WIDTH/2f*SENSOR_SIZEFACTOR, BODY_HEIGHT/2f*SENSOR_SIZEFACTOR);
		Vector2 subBoxOffset = new Vector2(BODY_WIDTH/4f*SENSOR_SIZEFACTOR, BODY_HEIGHT/4f*SENSOR_SIZEFACTOR);
		SolidContactSensor[] crawlSensors = spine.createCrawlSensors();
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.TOPRIGHT.ordinal()],
				subBoxOffset.x, subBoxOffset.y, subBoxSize.x, subBoxSize.y);
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.TOPLEFT.ordinal()],
				-subBoxOffset.x, subBoxOffset.y, subBoxSize.x, subBoxSize.y);
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.BOTTOMLEFT.ordinal()],
				-subBoxOffset.x, -subBoxOffset.y, subBoxSize.x, subBoxSize.y);
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.BOTTOMRIGHT.ordinal()],
				subBoxOffset.x, -subBoxOffset.y, subBoxSize.x, subBoxSize.y);
	}

	private void createCrawlSensorFixture(SolidContactSensor sensor, float posX, float posY, float sizeX,
			float sizeY) {
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, sensor,
				sizeX, sizeY, new Vector2(posX, posY));
	}

	public ContactDmgBrainContactFrameInput processContactFrame() {
		return new ContactDmgBrainContactFrameInput(spine.getCurrentRoom(), spine.isContactKeepAlive(),
				spine.isContactDespawn(), spine.getContactDmgTakeAgents());
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public void postUpdate() {
		prevPosition.set(b2body.getPosition());
	}

	public ZoomerSpine getSpine() {
		return spine;
	}
}
