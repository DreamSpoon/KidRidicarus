package kidridicarus.agent.body.Metroid.enemy;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.contact.CFBitSeq.CFBit;
import kidridicarus.agent.Agent;
import kidridicarus.agent.PlayerAgent;
import kidridicarus.agent.Metroid.enemy.Skree;
import kidridicarus.agent.body.MobileAgentBody;
import kidridicarus.agent.body.sensor.AgentContactSensor;
import kidridicarus.agent.body.sensor.OnGroundSensor;
import kidridicarus.info.UInfo;

public class SkreeBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16);
	private static final float BODY_HEIGHT = UInfo.P2M(16);
	private static final float FOOT_WIDTH = UInfo.P2M(18);
	private static final float FOOT_HEIGHT = UInfo.P2M(2);

	private Skree parent;
	private AgentContactSensor playerSensor;
	private OnGroundSensor ogSensor;

	public SkreeBody(Skree parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createAgentSensor();
		createPlayerSensor();
		createGroundSensor();
	}

	private void createBody(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, BODY_WIDTH, BODY_HEIGHT);
	}

	// same size as main body, for detecting agents
	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits,
				new AgentContactSensor(this)));
	}

	// cone shaped sensor extending down below skree to check for player target 
	private void createPlayerSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		float[] shapeVerts = new float[] {
				UInfo.P2M(24), UInfo.P2M(16),
				UInfo.P2M(-24), UInfo.P2M(16),
				UInfo.P2M(-80), UInfo.P2M(-176),
				UInfo.P2M(80), UInfo.P2M(-176) };
		boxShape.set(shapeVerts);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.AGENT_BIT);
		playerSensor = new AgentContactSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, playerSensor));
	}

	public Agent getPlayerContact() {
		return playerSensor.getFirstContactByClass(PlayerAgent.class);
	}

	// create the foot sensor for detecting onGround
	private void createGroundSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape;
		boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -BODY_HEIGHT/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SOLID_BOUND_BIT);
		ogSensor = new OnGroundSensor(null);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, ogSensor));
	}

	public boolean isOnGround() {
		// return true if the on ground contacts list contains at least 1 floor
		return ogSensor.isOnGround();
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
