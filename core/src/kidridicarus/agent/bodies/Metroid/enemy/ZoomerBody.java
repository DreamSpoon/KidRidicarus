package kidridicarus.agent.bodies.Metroid.enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contacts.CFBitSeq.CFBit;
import kidridicarus.agency.contacts.AgentBodyFilter;
import kidridicarus.agency.contacts.CFBitSeq;
import kidridicarus.agent.Metroid.enemy.Zoomer;
import kidridicarus.agent.bodies.CrawlAgentBody;
import kidridicarus.agent.bodies.sensor.CrawlSensor;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.GameInfo.DiagonalDir4;
import kidridicarus.info.UInfo;

public class ZoomerBody extends CrawlAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float SENSORSIZEFACTOR = 1.2f;

	private Zoomer parent;
	int[] contactCounts;

	public ZoomerBody(Zoomer parent, World world, Vector2 position) {
		this.parent = parent;

		contactCounts = new int[DiagonalDir4.values().length];
		for(int i=0; i<contactCounts.length; i++)
			contactCounts[i] = 0;

		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		createB2BodyAndFixture(world, position);

		Vector2 subBoxSize = new Vector2(BODY_WIDTH/2f*SENSORSIZEFACTOR,
				BODY_HEIGHT/2f*SENSORSIZEFACTOR);
		Vector2 subBoxOffset = new Vector2(BODY_WIDTH/4f*SENSORSIZEFACTOR,
				BODY_HEIGHT/4f*SENSORSIZEFACTOR);
		// Create 4 boxes, each slightly larger than 1/2 the size of the zoomer body
		// each of the boxes is assigned a quadrant (top-right, top-left, bottom-left, bottom-right).
		// Note: Give some thought to this when rotating the body, because the sensors will rotate with the body.
		//       So top-right for the body might not be top-right on screen.
		createCrawlSensor(subBoxOffset.x, subBoxOffset.y, subBoxSize.x, subBoxSize.y, DiagonalDir4.TOPRIGHT);
		createCrawlSensor(-subBoxOffset.x, subBoxOffset.y, subBoxSize.x, subBoxSize.y, DiagonalDir4.TOPLEFT);
		createCrawlSensor(-subBoxOffset.x, -subBoxOffset.y, subBoxSize.x, subBoxSize.y, DiagonalDir4.BOTTOMLEFT);
		createCrawlSensor(subBoxOffset.x, -subBoxOffset.y, subBoxSize.x, subBoxSize.y, DiagonalDir4.BOTTOMRIGHT);
	}

	private void createB2BodyAndFixture(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT, CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT, CFBit.GUIDE_SENSOR_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, BODY_WIDTH, BODY_HEIGHT);
	}

	// create the sensors for detecting walls to crawl on
	private void createCrawlSensor(float posX, float posY, float sizeX, float sizeY,
			DiagonalDir4 quad) {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footShape;
		footShape = new PolygonShape();
		footShape.setAsBox(sizeX/2f, sizeY/2f, new Vector2(posX, posY), 0f);
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_SENSOR_BIT, CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		fdef.shape = footShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, new CrawlSensor(this, quad)));
	}

	@Override
	public void onBeginContactWall(DiagonalDir4 quad, LineSeg lineSeg) {
		contactCounts[quad.ordinal()]++;
	}

	@Override
	public void onEndContactWall(DiagonalDir4 quad, LineSeg lineSeg) {
		contactCounts[quad.ordinal()]--;
	}

	public boolean isSensorContacting(DiagonalDir4 quad) {
		return contactCounts[quad.ordinal()] > 0;
	}

	@Override
	public Zoomer getParent() {
		return parent;
	}
}
