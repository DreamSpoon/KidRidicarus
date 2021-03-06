package kidridicarus.common.agent.followbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public abstract class FollowBoxBody extends AgentBody {
	// if the target position is at least this far away from the current position then reset the b2body
	// TODO: is 50 pixels right?
	private static final float RESET_DIST = UInfo.P2M(50);

	private MouseJoint mj;
	private boolean isSensor;

	protected abstract CFBitSeq getCatBits();
	protected abstract CFBitSeq getMaskBits();
	protected abstract Object getSensorBoxUserData();

	public FollowBoxBody(FollowBox parent, World world, Rectangle bounds, boolean isSensor) {
		super(parent, world);
		this.isSensor = isSensor;
		defineBody(bounds);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// destroy the old bodies if necessary
		if(mj != null && mj.getBodyA() != null) {
			// destroy the temp bodyA used by mouse joint, and the mouse joint
			world.destroyBody(mj.getBodyA());
		}
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(bounds.width, bounds.height);
		createRegBody(world, bounds, velocity, getCatBits(), getMaskBits());
		createMouseJoint(world, bounds.getCenter(new Vector2()));
	}

	private void createRegBody(World world, Rectangle bounds, Vector2 velocity, CFBitSeq catBits, CFBitSeq maskBits) {
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(0f);
		if(isSensor) {
			B2DFactory.makeSensorBoxFixture(b2body, catBits, maskBits, getSensorBoxUserData(),
					bounds.width, bounds.height);
		}
		else
			B2DFactory.makeBoxFixture(b2body, catBits, maskBits, getSensorBoxUserData(), bounds.width, bounds.height);
	}

	// mouse joint allows body to quickly change position without destroying/recreating the body/fixture constantly
	private void createMouseJoint(World world, Vector2 position) {
		// TODO: find a better place to stick this temp body 
		Body tempB = B2DFactory.makeDynamicBody(world, new Vector2(0f, 0f));
		tempB.setGravityScale(0f);

		// the fake body does not contact anything
		B2DFactory.makeSensorBoxFixture(tempB, CommonCF.NO_CONTACT_CFCAT, CommonCF.NO_CONTACT_CFMASK, this,
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
		Vector2 diff = position.cpy().sub(b2body.getPosition());
		if(diff.len() >= RESET_DIST)
			resetFollowBoxPosition(position);
		else
			mj.setTarget(position);
	}

	private void resetFollowBoxPosition(Vector2 position) {
		Rectangle oldBounds = getBounds();
		defineBody(new Rectangle(position.x - oldBounds.width/2f,
				position.y - oldBounds.height/2f, oldBounds.width, oldBounds.height));
	}

	@Override
	public void dispose() {
		world.destroyBody(mj.getBodyA());	// destroy the temp bodyA used by mouse joint
		super.dispose();
	}
}
