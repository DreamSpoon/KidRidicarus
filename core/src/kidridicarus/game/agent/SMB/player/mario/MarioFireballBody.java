package kidridicarus.game.agent.SMB.player.mario;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentsensor.AgentContactHoldSensor;
import kidridicarus.common.agentsensor.SolidBoundSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MarioFireballBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);

	private static final CFBitSeq MAIN_CFCAT = CommonCF.SOLID_BODY_CFCAT;
	private static final CFBitSeq MAIN_CFMASK = CommonCF.SOLID_BODY_CFMASK;
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT);

	private MarioFireball parent;
	private SolidBoundSensor hmSensor;
	private AgentContactHoldSensor acSensor;
	private Fixture acSensorFixture;

	public MarioFireballBody(MarioFireball parent, World world, Vector2 position, Vector2 velocity) {
		this.parent = parent;
		defineBody(world, position, velocity);
	}

	private void defineBody(World world, Vector2 position, Vector2 velocity) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);

		createBody(world, position, velocity);
		createAgentSensor();
	}

	private void createBody(World world, Vector2 position, Vector2 velocity) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.linearVelocity.set(velocity);
		bdef.gravityScale = 2f;	// heavy
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0f;		// slippery
		fdef.restitution = 1f;	// bouncy
		hmSensor = new SolidBoundSensor(parent);
		B2DFactory.makeBoxFixture(b2body, fdef, hmSensor,
				MAIN_CFCAT, MAIN_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.isSensor = true;
		fdef.shape = boxShape;
		acSensor = new AgentContactHoldSensor(this);
		acSensorFixture = b2body.createFixture(fdef);
		acSensorFixture.setUserData(new AgentBodyFilter(AS_CFCAT, AS_CFMASK,
				acSensor));
	}

	public List<Agent> getContactAgents() {
		return acSensor.getContacts();
	}

	public <T> List<T> getContactAgentsByClass(Class<T> cls) {
		return acSensor.getContactsByClass(cls);
	}

	public boolean isMoveBlocked(boolean movingRight) {
		return hmSensor.isHMoveBlocked(getBounds(), movingRight);
	}

	public void setGravityScale(float scale) {
		b2body.setGravityScale(scale);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
