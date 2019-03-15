package kidridicarus.game.agent.SMB.player.luigi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.MobileAgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class LuigiBody extends MobileAgentBody {
	private static final Vector2 BIG_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(26f));
	private static final Vector2 SML_BODY_SIZE = new Vector2(UInfo.P2M(14f), UInfo.P2M(12f));
	private static final float FOOT_WIDTH = UInfo.P2M(5f);
	private static final float FOOT_HEIGHT = UInfo.P2M(2f);
	// TODO head size needs some work, bump tile is inconsistent
	private static final float HEAD_WIDTH = UInfo.P2M(10f);
	private static final float HEAD_HEIGHT = UInfo.P2M(12f);

	// main body
	private static final CFBitSeq MAINBODY_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAINBODY_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	// agent sensor
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.ROOM_BIT, CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.POWERUP_BIT);
	// ground sensor
	private static final CFBitSeq GROUND_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq GROUND_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_BOUND_BIT);
	// bumptile sensor
	private static final CFBitSeq BUMPTILE_SENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq BUMPTILE_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);

	private static final float FRICTION = 0f;
	private static final float GRAVITY_SCALE = 2f;

	private Luigi parent;
	private World world;
	private LuigiSpine spine;
	private Vector2 prevVelocity;

	public LuigiBody(Luigi parent, World world, Vector2 position, Vector2 velocity, boolean isBigBody,
			boolean isDucking) {
		this.parent = parent;
		this.world = world;
		defineBody(position, velocity, isBigBody, isDucking);
	}

	public void defineBody(Vector2 position, Vector2 velocity, boolean isBigBody, boolean isDucking) {
		if(isBigBody && !isDucking)
			setBodySize(BIG_BODY_SIZE.x, BIG_BODY_SIZE.y);
		else
			setBodySize(SML_BODY_SIZE.x, SML_BODY_SIZE.y);

		createBody(position, velocity);
		createSpineAndSensors();
	}

	private void createBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		
		prevVelocity = new Vector2(0f, 0f);

		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = GRAVITY_SCALE;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = FRICTION;
		B2DFactory.makeBoxFixture(b2body, fdef, this, MAINBODY_CFCAT, MAINBODY_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	private void createSpineAndSensors() {
		spine = new LuigiSpine(this);
		createAgentSensorFixture();
		createGroundSensorFixture();
		createBumpTileSensorFixture();
	}

	private void createAgentSensorFixture() {
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(), AS_CFCAT, AS_CFMASK,
				getBodySize().x, getBodySize().y);
	}

	private void createGroundSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(FOOT_WIDTH/2f, FOOT_HEIGHT/2f, new Vector2(0f, -getBodySize().y/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(GROUND_SENSOR_CFCAT, GROUND_SENSOR_CFMASK,
				spine.createOnGroundSensor()));
	}

	// head sensor for detecting head hits against bumptiles
	private void createBumpTileSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(HEAD_WIDTH/2f, HEAD_HEIGHT/2f, new Vector2(0f, getBodySize().y/2f), 0f);
		fdef.shape = boxShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(BUMPTILE_SENSOR_CFCAT, BUMPTILE_SENSOR_CFMASK,
				spine.createBumpTileSensor()));
	}

	public void postUpdate() {
		prevVelocity.set(b2body.getLinearVelocity());
	}

	public void applyBodyImpulse (Vector2 impulse) {
		b2body.applyLinearImpulse(impulse, b2body.getWorldCenter(), true);
	}

	public Vector2 getPrevVelocity() {
		return prevVelocity;
	}

	public LuigiSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
