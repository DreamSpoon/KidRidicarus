package kidridicarus.common.tool;

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
 * Convenience class for Box2D body/fixture creation.
 */
public class B2DFactory {
	public static Body makeDynamicBody(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(position);
		return world.createBody(bdef);
	}

	public static Body makeStaticBody(World world, Vector2 position) {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.set(position);
		return world.createBody(bdef);
	}

	public static Fixture makeBoxFixture(Body b2body, FixtureDef fdef, Object userData,
			CFBitSeq categoryBits, CFBitSeq maskBits, float width, float height, Vector2 position) {
		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(width/2f, height/2f, position, 0f);
		fdef.shape = boxShape;
		Fixture fix = b2body.createFixture(fdef);
		fix.setUserData(new AgentBodyFilter(categoryBits, maskBits, userData));
		return fix;
	}

	public static Fixture makeBoxFixture(Body b2body, FixtureDef fdef, Object userData,
			CFBitSeq categoryBits, CFBitSeq maskBits, float width, float height) {
		return makeBoxFixture(b2body, fdef, userData, categoryBits, maskBits, width, height,
				new Vector2(0f, 0f));
	}

	public static Fixture makeSensorBoxFixture(Body b2body, Object userData,
			CFBitSeq categoryBits, CFBitSeq maskBits, float width, float height) {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		return makeBoxFixture(b2body, fdef, userData, categoryBits, maskBits, width, height);
	}

	public static Fixture makeSensorBoxFixture(Body b2body, Object userData,
			CFBitSeq categoryBits, CFBitSeq maskBits, float width, float height, Vector2 position) {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		return makeBoxFixture(b2body, fdef, userData, categoryBits, maskBits, width, height,
				position);
	}
}
