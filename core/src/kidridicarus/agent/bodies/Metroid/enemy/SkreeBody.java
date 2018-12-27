package kidridicarus.agent.bodies.Metroid.enemy;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contacts.AgentBodyFilter;
import kidridicarus.agency.contacts.CFBitSeq;
import kidridicarus.agency.contacts.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;
import kidridicarus.agent.Metroid.enemy.Skree;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.bodies.MobileAgentBody;
import kidridicarus.agent.bodies.sensor.FindGuideAgentCallback;
import kidridicarus.agent.bodies.sensor.FindGuideAgentSensor;
import kidridicarus.agent.bodies.sensor.LineSegContactSensor;
import kidridicarus.collisionmap.LineSeg;
import kidridicarus.info.UInfo;

public class SkreeBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16);
	private static final float BODY_HEIGHT = UInfo.P2M(16);
	private static final float FOOT_WIDTH = UInfo.P2M(18);
	private static final float FOOT_HEIGHT = UInfo.P2M(2);

	private Skree parent;

	public SkreeBody(Skree parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
		createGuideSensorFixture();
		createGroundSensorFixture();
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT, CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT, CFBit.AGENT_BIT, CFBit.GUIDE_SENSOR_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, BODY_WIDTH, BODY_HEIGHT);
	}

	private void createGuideSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footShape;
		footShape = new PolygonShape();
		float[] shapeVerts = new float[] {
				UInfo.P2M(24), UInfo.P2M(16),
				UInfo.P2M(-24), UInfo.P2M(16),
				UInfo.P2M(-80), UInfo.P2M(-176),
				UInfo.P2M(80), UInfo.P2M(-176) };
		footShape.set(shapeVerts);
		fdef.shape = footShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_SENSOR_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.GUIDE_BIT);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, new FindGuideAgentSensor(
				new FindGuideAgentCallback() {
					@Override
					public void onBeginContactGuideAgent(AgentBody mBody) { setTarget(mBody); }
					@Override
					public void onEndContactGuideAgent(AgentBody mBody) {}
				})));
	}

	// create the foot sensor for detecting onGround
	private void createGroundSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape footShape;
		footShape = new PolygonShape();
		footShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = footShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.SOLID_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits,
				new LineSegContactSensor() {
					@Override
					public void onBeginContact(LineSeg lineSeg) {
						// ensure that the lineSeg is a floor
						if(lineSeg.isHorizontal && lineSeg.upNormal == true)
							hitGround();
					}

					@Override
					public void onEndContact(LineSeg lineSeg) {
					}
				}));
	}

	private void setTarget(AgentBody mBody) {
		parent.setTarget(mBody.getParent());
	}
	
	private void hitGround() {
		parent.hitGround();
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
