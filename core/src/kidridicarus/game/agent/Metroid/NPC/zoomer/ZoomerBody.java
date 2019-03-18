package kidridicarus.game.agent.Metroid.NPC.zoomer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.DiagonalDir4;

public class ZoomerBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float SENSOR_SIZEFACTOR = 1.2f;
	private static final float GRAVITY_SCALE = 0f;

	private Zoomer parent;
	private ZoomerSpine spine;
	private Vector2 prevPosition;

	public ZoomerBody(Zoomer parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createFixtures();
	}

	private void createBody(World world, Vector2 position) {
		prevPosition = position.cpy();
		b2body = B2DFactory.makeDynamicBody(world, position);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new ZoomerSpine(this);
	}

	private void createFixtures() {
		createMainFixture();
		createAgentSensor();
		createCrawlSensorFixtures();
	}

	private void createMainFixture() {
		B2DFactory.makeBoxFixture(b2body, this, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.AGENT_SENSOR_CFCAT, CommonCF.AGENT_SENSOR_CFMASK, getBodySize().x, getBodySize().y);
	}

	private void createCrawlSensorFixtures() {
		Vector2 subBoxSize = new Vector2(BODY_WIDTH/2f*SENSOR_SIZEFACTOR, BODY_HEIGHT/2f*SENSOR_SIZEFACTOR);
		Vector2 subBoxOffset = new Vector2(BODY_WIDTH/4f*SENSOR_SIZEFACTOR, BODY_HEIGHT/4f*SENSOR_SIZEFACTOR);
		SolidBoundSensor[] crawlSensors = spine.createCrawlSensors();
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.TOPRIGHT.ordinal()],
				subBoxOffset.x, subBoxOffset.y, subBoxSize.x, subBoxSize.y);
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.TOPLEFT.ordinal()],
				-subBoxOffset.x, subBoxOffset.y, subBoxSize.x, subBoxSize.y);
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.BOTTOMLEFT.ordinal()],
				-subBoxOffset.x, -subBoxOffset.y, subBoxSize.x, subBoxSize.y);
		createCrawlSensorFixture(crawlSensors[DiagonalDir4.BOTTOMRIGHT.ordinal()],
				subBoxOffset.x, -subBoxOffset.y, subBoxSize.x, subBoxSize.y);
	}

	private void createCrawlSensorFixture(SolidBoundSensor sensor, float posX, float posY, float sizeX, float sizeY) {
		B2DFactory.makeSensorBoxFixture(b2body, sensor, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				sizeX, sizeY, new Vector2(posX, posY));
	}

	public void postUpdate() {
		prevPosition.set(b2body.getPosition());
	}

	public Vector2 getPrevPosition() {
		return prevPosition;
	}

	public ZoomerSpine getSpine() {
		return spine;
	}

	@Override
	public Zoomer getParent() {
		return parent;
	}
}
