package kidridicarus.game.Metroid.agent.NPC.skree;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agentbrain.ContactDmgBrainContactFrameInput;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

class SkreeBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16);
	private static final float BODY_HEIGHT = UInfo.P2M(18);
	private static final float FOOT_WIDTH = UInfo.P2M(18);
	private static final float FOOT_HEIGHT = UInfo.P2M(2);
	private static final float[] PLAYER_DETECTOR_SHAPE = new float[] {
			UInfo.P2M(24), UInfo.P2M(16),
			UInfo.P2M(-24), UInfo.P2M(16),
			UInfo.P2M(-80), UInfo.P2M(-176),
			UInfo.P2M(80), UInfo.P2M(-176) };

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT,
			CommonCF.Alias.DESPAWN_BIT, CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.ROOM_BIT);

	private SkreeSpine spine;

	SkreeBody(Skree parent, World world, Vector2 position, Vector2 velocity) {
		super(parent, world);
		defineBody(new Rectangle(position.x-BODY_WIDTH/2f, position.y-BODY_HEIGHT/2f, BODY_WIDTH, BODY_HEIGHT),
				velocity);
	}

	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);
		// set body size info and create new body
		setBoundsSize(bounds.getWidth(), bounds.getHeight());
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()), velocity);
		b2body.setGravityScale(0f);
		spine = new SkreeSpine(this);
		createFixtures();
	}

	private void createFixtures() {
		// main fixture
		B2DFactory.makeBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK, this,
				getBounds().width, getBounds().height);
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, AS_CFCAT, AS_CFMASK, spine.createAgentSensor(),
				getBounds().width, getBounds().height);
		// player sensor fixture
		createPlayerSensorFixture();
		// on ground sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, CommonCF.SOLID_BODY_CFCAT, CommonCF.SOLID_BODY_CFMASK,
				spine.createSolidContactSensor(), FOOT_WIDTH, FOOT_HEIGHT, new Vector2(0f, -getBounds().height/2f));
	}

	// cone shaped sensor extending down below skree to check for player target 
	private void createPlayerSensorFixture() {
		FixtureDef fdef = new FixtureDef();
		PolygonShape coneShape;
		coneShape = new PolygonShape();
		coneShape.set(PLAYER_DETECTOR_SHAPE);
		fdef.shape = coneShape;
		fdef.isSensor = true;
		b2body.createFixture(fdef).setUserData(new AgentBodyFilter(CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, spine.createPlayerSensor()));
	}

	ContactDmgBrainContactFrameInput processContactFrame() {
		return new ContactDmgBrainContactFrameInput(spine.getCurrentRoom(), spine.isContactKeepAlive(),
				spine.isContactDespawn(), spine.getContactDmgTakeAgents());
	}

	SkreeSpine getSpine() {
		return spine;
	}
}
