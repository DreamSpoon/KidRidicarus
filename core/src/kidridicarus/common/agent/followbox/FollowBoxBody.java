package kidridicarus.common.agent.followbox;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
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
	protected void defineBody(Rectangle bounds) {
		// destroy the old bodies if necessary
		if(mj != null && mj.getBodyA() != null)
			world.destroyBody(mj.getBodyA());	// destroy the temp bodyA used by mouse joint
		if(b2body != null)
			world.destroyBody(b2body);

		setBodySize(bounds.width, bounds.height);
		createRegBody(world, bounds, getCatBits(), getMaskBits());
		createMouseJoint(world, bounds.getCenter(new Vector2()));
	}

	private void createRegBody(World world, Rectangle bounds, CFBitSeq catBits, CFBitSeq maskBits) {
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(0f);
		if(isSensor) {
			B2DFactory.makeSensorBoxFixture(b2body, getSensorBoxUserData(), catBits, maskBits,
				bounds.width, bounds.height);
		}
		else {
			B2DFactory.makeBoxFixture(b2body, getSensorBoxUserData(), catBits, maskBits,
					bounds.width, bounds.height);
		}
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
		Vector2 diff = position.cpy().sub(b2body.getPosition());
		if(diff.len() >= RESET_DIST)
			resetPosition(position);
		else
			mj.setTarget(position);
	}

	private void resetPosition(Vector2 position) {
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
