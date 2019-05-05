package kidridicarus.desktop;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.common.tool.QQ;

public class TestContacts {
	private World world;
	private int stepCount = 0;

	public TestContacts() {
QQ.pr("--- start test ---");
		runTest();
QQ.pr("--- end test ---");
	}

	public void runTest() {
		init();

		runStep();
		runStep();
		Body box1 = createBox(new Rectangle(0f, 0f, 10f, 10f));
		runStep();
		runStep();
		Body box2 = createBox(new Rectangle(0f, 0f, 10f, 10f));
		runStep();
		runStep();
		changeBoxFilter(box1, (short) 16, (short) 16);
		runStep();
		runStep();
		destroyBox(box1);
		runStep();
		runStep();
		Body box3 = createBox(new Rectangle(0f, 0f, 10f, 10f));
		runStep();
		runStep();
		Body box4 = createBox(new Rectangle(0f, 0f, 10f, 10f));
		destroyBox(box4);
		runStep();
		runStep();
		destroyBox(box3);
		runStep();
		runStep();
		destroyBox(box2);
		runStep();
		runStep();
QQ.pr("post final step");
	}

	private void changeBoxFilter(Body boxBody, short catBits, short maskBits) {
QQ.pr("pre-change box filter, box="+boxBody+", cat="+catBits+", mask="+maskBits);
		for(Fixture fix : boxBody.getFixtureList()) {
			Filter filter = new Filter();
			filter.categoryBits = catBits;
			filter.maskBits = maskBits;
			fix.setFilterData(filter);
			fix.refilter();
		}
QQ.pr("post-change box filter, box="+boxBody+", cat="+catBits+", mask="+maskBits);
	}

	private void init() {
		world = new World(new Vector2(0, 0f), true);
		world.setContactListener(new ContactListener() {
				@Override
				public void beginContact(Contact contact) {
QQ.pr("begin contact=" + contactToString(contact));
				}
	
				@Override
				public void endContact(Contact contact) {
QQ.pr("end contact=" + contactToString(contact));
				}
	
				@Override
				public void preSolve(Contact contact, Manifold oldManifold) {
QQ.pr("preSolve contact=" + contactToString(contact));
				}
	
				@Override
				public void postSolve(Contact contact, ContactImpulse impulse) {
QQ.pr("postSolve contact=" + contactToString(contact));
contactToString(contact);
				}
			});
	}

	private void runStep() {
QQ.pr("   --- start step #" + stepCount + " ---");
		world.step(1/60f, 6, 4);
QQ.pr("   --- end step #" + stepCount + " ---");
		stepCount++;
	}

	private Body createBox(Rectangle bounds) {
QQ.pr("pre-create box, bounds=" + bounds);
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(bounds.getCenter(new Vector2()));
		Body boxBody = world.createBody(bdef);
		FixtureDef fdef = new FixtureDef();
		fdef.filter.categoryBits = 1;
		fdef.filter.maskBits = 1;
		PolygonShape shp = new PolygonShape();
		fdef.shape = shp;

		PolygonShape boxShape = new PolygonShape();
		boxShape.setAsBox(bounds.width/2f, bounds.height/2f);
		fdef.shape = boxShape;
		boxBody.createFixture(fdef);
QQ.pr("post-create box="+boxBody+", bounds=" + bounds);
		return boxBody;
	}

	private void destroyBox(Body boxBody) {
QQ.pr("pre-destroy box="+boxBody);
		world.destroyBody(boxBody);
QQ.pr("post-destroy box="+boxBody);
	}

	private String contactToString(Contact contact) {
		return "contact-(fixA="+contact.getFixtureA()+", fixB="+contact.getFixtureB()+")";
	}
}
