package kidridicarus.common.tool;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import kidridicarus.agency.agentcontact.AgentBodyFilter;
import kidridicarus.agency.agentcontact.CFBitSeq;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/*
 * Convenience class for Box2D body creation.
 */
public class B2DFactory {
	public static Body makeBoxBody(World world, BodyType bodytype, Object userData, CFBitSeq categoryBits,
			CFBitSeq maskBits, Rectangle bounds) {
		return makeBoxBody(world, bodytype, userData, categoryBits, maskBits, bounds.getCenter(new Vector2()),
				bounds.width, bounds.height);
	}

	public static Body makeBoxBody(World world, BodyType bodytype, Object userData, CFBitSeq categoryBits,
			CFBitSeq maskBits, Vector2 position, float width, float height) {
		Body b2body;
		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = bodytype;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(width/2f, height/2f);
		fdef.shape = boxShape;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(categoryBits, maskBits, userData));

		return b2body;
	}

	public static Body makeSpecialBoxBody(World world, BodyDef bdef, FixtureDef fdef, Object userData,
			CFBitSeq categoryBits, CFBitSeq maskBits, float width, float height) {
		Body b2body;
		b2body = world.createBody(bdef);

		FixtureDef boxFdef = new FixtureDef();
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(width/2f, height/2f);
		boxFdef.shape = boxShape;
		boxFdef.friction = fdef.friction;
		boxFdef.restitution = fdef.restitution;
		boxFdef.density = fdef.density;
		boxFdef.isSensor = fdef.isSensor;
		b2body.createFixture(boxFdef).setUserData(new AgentBodyFilter(categoryBits, maskBits, userData));

		return b2body;
	}

	// TODO: replace the above methods - split into body and fixture creation

	public static Fixture makeBoxFixture(Body b2body, FixtureDef fdef, Object userData,
			CFBitSeq categoryBits, CFBitSeq maskBits, float width, float height) {
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(width/2f, height/2f);
		fdef.shape = boxShape;
		Fixture fix = b2body.createFixture(fdef);
		fix.setUserData(new AgentBodyFilter(categoryBits, maskBits, userData));
		return fix;
	}

}
