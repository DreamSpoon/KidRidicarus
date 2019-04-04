package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MonoeyeBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	// Contact spawn trigger to detect screen scroll (TODO create a ScreenAgent that represents the player screen
	// and allow this body to contact ScreenAgent?). 
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.SPAWNTRIGGER_BIT);

	private MonoeyeSpine spine;
	private MouseJoint mj;

	public MonoeyeBody(Monoeye parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(position, velocity);
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// destroy the old bodies if necessary
		if(mj != null && mj.getBodyA() != null) {
			// destroy the temp bodyA used by mouse joint, and the mouse joint
			world.destroyBody(mj.getBodyA());
		}
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new MonoeyeSpine(this);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(), AS_CFCAT, AS_CFMASK,
				BODY_WIDTH, BODY_HEIGHT);
		// mouse joint to ensure position can be set directly, without having to fudge bodies
		createMouseJoint(world, position);
	}

	// mouse joint allows body to quickly change position without destroying/recreating the body/fixture constantly
	private void createMouseJoint(World world, Vector2 position) {
		// TODO: find a better place to stick this temp body 
		Body tempB = B2DFactory.makeDynamicBody(world, new Vector2(0f, 0f));
		tempB.setGravityScale(0f);

		// the fake body does not contact anything
		B2DFactory.makeSensorBoxFixture(tempB, this, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK,
				0.01f, 0.01f);

		MouseJointDef mjdef = new MouseJointDef();
		// this body is supposedly ignored by box2d, but needs to be a valid non-static body (non-sensor also?)
		mjdef.bodyA = tempB;
		// this is the body that will move to "catch up" to the mouse joint target
		mjdef.bodyB = b2body;
		mjdef.maxForce = 5000f * b2body.getMass();
		mjdef.frequencyHz = 5f;
		mjdef.dampingRatio = 0.9f;
		mjdef.target.set(position);
		mj = (MouseJoint) world.createJoint(mjdef);
		mj.setTarget(position);
	}

	public void setPosition(Vector2 position) {
		mj.setTarget(position);
	}

	public MonoeyeSpine getSpine() {
		return spine;
	}
}
