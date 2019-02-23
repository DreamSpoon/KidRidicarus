package kidridicarus.game.agent.body.Metroid.NPC;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;

import kidridicarus.agency.agent.body.MobileAgentBody;
import kidridicarus.agency.agent.body.sensor.AgentContactSensor;
import kidridicarus.agency.agent.body.sensor.SolidBoundSensor;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.game.agent.Metroid.NPC.Zoomer;
import kidridicarus.game.info.GameInfo.DiagonalDir4;

public class ZoomerBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float SENSORSIZEFACTOR = 1.2f;

	private Zoomer parent;
	private SolidBoundSensor[] crawlSense;
	private int[] contactCounts;

	public ZoomerBody(Zoomer parent, World world, Vector2 position) {
		this.parent = parent;
		// 4 sensors: top-right, top-left, bottom-left, bottom-right
		crawlSense = new SolidBoundSensor[] { null, null, null, null };

		contactCounts = new int[DiagonalDir4.values().length];
		for(int i=0; i<contactCounts.length; i++)
			contactCounts[i] = 0;

		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		createBody(world, position);
		createAgentSensor();

		Vector2 subBoxSize = new Vector2(BODY_WIDTH/2f*SENSORSIZEFACTOR,
				BODY_HEIGHT/2f*SENSORSIZEFACTOR);
		Vector2 subBoxOffset = new Vector2(BODY_WIDTH/4f*SENSORSIZEFACTOR,
				BODY_HEIGHT/4f*SENSORSIZEFACTOR);
		// Create 4 sensor boxes, each slightly larger than 1/2 the size of the zoomer body. Each of the boxes is
		// assigned a quadrant (top-right, top-left, bottom-left, bottom-right).
		// Note: Give some thought to this when rotating the body (or just don't rotate the body!), because the
		//       sensors will rotate with the body. So top-right for the body might not be top-right on screen.
		crawlSense[DiagonalDir4.TOPRIGHT.ordinal()] = createCrawlSensor(subBoxOffset.x, subBoxOffset.y,
				subBoxSize.x, subBoxSize.y);
		crawlSense[DiagonalDir4.TOPLEFT.ordinal()] = createCrawlSensor(-subBoxOffset.x, subBoxOffset.y,
				subBoxSize.x, subBoxSize.y);
		crawlSense[DiagonalDir4.BOTTOMLEFT.ordinal()] = createCrawlSensor(-subBoxOffset.x, -subBoxOffset.y,
				subBoxSize.x, subBoxSize.y);
		crawlSense[DiagonalDir4.BOTTOMRIGHT.ordinal()] = createCrawlSensor(subBoxOffset.x, -subBoxOffset.y,
				subBoxSize.x, subBoxSize.y);
	}

	private void createBody(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.friction = 0.001f;
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits,
				new AgentContactSensor(this)));
	}

	// create the sensors for detecting walls to crawl on
	private SolidBoundSensor createCrawlSensor(float posX, float posY, float sizeX, float sizeY) {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(sizeX/2f, sizeY/2f, new Vector2(posX, posY), 0f);
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		SolidBoundSensor sensor = new SolidBoundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, sensor));
		return sensor;
	}

	public boolean isSensorContacting(DiagonalDir4 quad) {
		return !crawlSense[quad.ordinal()].getContacts().isEmpty();
	}

	@Override
	public Zoomer getParent() {
		return parent;
	}
}
