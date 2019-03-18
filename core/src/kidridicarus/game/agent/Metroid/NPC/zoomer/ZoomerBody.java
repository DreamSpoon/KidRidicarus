package kidridicarus.game.agent.Metroid.NPC.zoomer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.common.tool.DiagonalDir4;

public class ZoomerBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float SENSORSIZEFACTOR = 1.2f;

	private Zoomer parent;
	private ZoomerSpine spine;

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
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0f;
		b2body = world.createBody(bdef);

		spine = new ZoomerSpine(this);
	}

	private void createFixtures() {
		createMainFixture();
		createAgentSensor();
		createCrawlSensorFixtures();
	}

	private void createMainFixture() {
		FixtureDef fdef = new FixtureDef();
		B2DFactory.makeBoxFixture(b2body, fdef, this, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, new AgentContactHoldSensor(this)));
	}

	private void createCrawlSensorFixtures() {
		Vector2 subBoxSize = new Vector2(BODY_WIDTH/2f*SENSORSIZEFACTOR,
				BODY_HEIGHT/2f*SENSORSIZEFACTOR);
		Vector2 subBoxOffset = new Vector2(BODY_WIDTH/4f*SENSORSIZEFACTOR,
				BODY_HEIGHT/4f*SENSORSIZEFACTOR);
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

	private void createCrawlSensorFixture(SolidBoundSensor sensor, float posX, float posY,
			float sizeX, float sizeY) {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(sizeX/2f, sizeY/2f, new Vector2(posX, posY), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.SOLID_BODY_CFCAT,
				CommonCF.SOLID_BODY_CFMASK, sensor));
	}

	public ZoomerSpine getSpine() {
		return spine;
	}

	@Override
	public Zoomer getParent() {
		return parent;
	}
}
