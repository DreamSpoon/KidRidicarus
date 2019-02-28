package kidridicarus.game.agent.body.SMB.player;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.body.MobileAgentBody;
import kidridicarus.agency.agent.body.sensor.AgentContactSensor;
import kidridicarus.agency.agent.body.sensor.SolidBoundSensor;
import kidridicarus.agency.contact.AgentBodyFilter;
import kidridicarus.agency.contact.CFBitSeq;
import kidridicarus.agency.info.UInfo;
import kidridicarus.agency.tool.B2DFactory;
import kidridicarus.game.agent.SMB.player.MarioFireball;
import kidridicarus.game.info.GameInfo;

public class MarioFireballBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(7f);
	private static final float BODY_HEIGHT = UInfo.P2M(7f);

	private MarioFireball parent;
	private SolidBoundSensor hmSensor;
	private AgentContactSensor acSensor;

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
		FixtureDef fdef = new FixtureDef();
		fdef.friction = 0f;		// slippery
		fdef.restitution = 1f;	// bouncy
		CFBitSeq catBits = new CFBitSeq(GameInfo.CFBits.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(GameInfo.CFBits.SOLID_BOUND_BIT);
		hmSensor = new SolidBoundSensor(parent);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, hmSensor, catBits, maskBits,
				BODY_WIDTH, BODY_HEIGHT);
	}

	private void createAgentSensor() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(BODY_WIDTH/2f, BODY_HEIGHT/2f);
		fdef.isSensor = true;
		fdef.shape = boxShape;
		CFBitSeq catBits = new CFBitSeq(GameInfo.CFBits.AGENT_BIT);
		CFBitSeq maskBits = new CFBitSeq(GameInfo.CFBits.AGENT_BIT);
		acSensor = new AgentContactSensor(this);
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(catBits, maskBits, acSensor));
	}

	public List<Agent> getContactAgents() {
		return acSensor.getContacts();
	}

	public <T> List<Agent> getContactAgentsByClass(Class<T> clazz) {
		return acSensor.getContactsByClass(clazz);
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
