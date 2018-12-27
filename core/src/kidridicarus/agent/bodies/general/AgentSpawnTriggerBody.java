package kidridicarus.agent.bodies.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.B2DFactory;
import kidridicarus.agency.contacts.CFBitSeq;
import kidridicarus.agency.contacts.CFBitSeq.CFBit;
import kidridicarus.agent.bodies.AgentBody;
import kidridicarus.agent.general.AgentSpawnTrigger;

public class AgentSpawnTriggerBody extends AgentBody implements Disposable {
	private AgentSpawnTrigger parent;
	private Body b2body;
	private MouseJoint mj;

	public AgentSpawnTriggerBody(AgentSpawnTrigger parent, World world, Rectangle bounds) {
		this.parent = parent;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		createRegBody(world, bounds);
		createMouseJoint(world, bounds);
	}

	private void createRegBody(World world, Rectangle bounds) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(bounds.getCenter(new Vector2()));
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq(CFBit.SPAWNTRIGGER_BIT);
		CFBitSeq maskBits = new CFBitSeq(CFBit.SPAWNBOX_BIT);
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, bounds.width, bounds.height);
	}

	// mouse joint allows us to quickly change the position of the spawn trigger body without breaking Box2D
	private void createMouseJoint(World world, Rectangle bounds) {
		Body tempB;	// mouse joint body

		// mouse joint needs a fake body, so create a sensor body with no gravity
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.position.set(0f, 0f);
		bdef.gravityScale = 0f;
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		CFBitSeq catBits = new CFBitSeq();
		CFBitSeq maskBits = new CFBitSeq();
		tempB = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, catBits, maskBits, 1f, 1f);

		MouseJointDef mjdef = new MouseJointDef();
		// this body is supposedly ignored by box2d, but needs to be a valid non-static body (non-sensor also?)
		mjdef.bodyA = tempB;
		// this is the body that will move to "catch up" to the mouse joint target
		mjdef.bodyB = b2body;
		mjdef.maxForce = 5000f * b2body.getMass();
		mjdef.frequencyHz = 5f;
		mjdef.dampingRatio = 0.9f;
		mjdef.target.set(bounds.getCenter(new Vector2()));
		mj = (MouseJoint) world.createJoint(mjdef);
	}

	public void setPosition(Vector2 position) {
		mj.setTarget(position);
	}

	public void onBeginContactSpawnBox(AgentSpawnerBody sbBody) {
		parent.onBeginContactSpawnBox(sbBody.getParent());
	}

	public void onEndContactSpawnBox(AgentSpawnerBody sbBody) {
		parent.onEndContactSpawnBox(sbBody.getParent());
	}

	@Override
	public AgentSpawnTrigger getParent() {
		return parent;
	}

	@Override
	public void dispose() {
		b2body.getWorld().destroyBody(mj.getBodyA());	// destroy the temp bodyA used by mouse joint
		b2body.getWorld().destroyJoint(mj);
		b2body.getWorld().destroyBody(b2body);
	}
}
