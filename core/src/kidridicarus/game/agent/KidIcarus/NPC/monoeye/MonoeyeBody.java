package kidridicarus.game.agent.KidIcarus.NPC.monoeye;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.MobileAgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class MonoeyeBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float PLAYER_SENSOR_WIDTH = UInfo.P2M(128);
	private static final float PLAYER_SENSOR_HEIGHT = UInfo.P2M(176);
	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	// Contact spawn trigger to detect screen scroll (TODO create a ScreenAgent that represents the player screen
	// and allow this body to contact ScreenAgent?). 
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.SPAWNTRIGGER_BIT);

	private MonoeyeSpine spine;

	public MonoeyeBody(Monoeye parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(position, new Vector2(0f, 0f));
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new MonoeyeSpine(this, UInfo.M2Tx(position.x));
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(), AS_CFCAT, AS_CFMASK,
				getBodySize().x, getBodySize().y);
		// create player sensor fixture that "hangs" down from the top of Monoeye and detects players to target
		B2DFactory.makeSensorBoxFixture(b2body, spine.createPlayerSensor(), CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, PLAYER_SENSOR_WIDTH, PLAYER_SENSOR_HEIGHT,
				new Vector2(0f, getBodySize().y/2f - PLAYER_SENSOR_HEIGHT/2f));
	}

	public MonoeyeSpine getSpine() {
		return spine;
	}
}
/*
public class MonoeyeBody extends MobileAgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(12f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);
	private static final float PLAYER_SENSOR_WIDTH = UInfo.P2M(128);
	private static final float PLAYER_SENSOR_HEIGHT = UInfo.P2M(176);
	private static final float GRAVITY_SCALE = 0f;

	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	// Contact spawn trigger to detect screen scroll (TODO create a ScreenAgent that represents the player screen
	// and allow this body to contact ScreenAgent?). 
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT, CommonCF.Alias.DESPAWN_BIT,
			CommonCF.Alias.KEEP_ALIVE_BIT, CommonCF.Alias.SPAWNTRIGGER_BIT);

	private MonoeyeSpine spine;

	public MonoeyeBody(Monoeye parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(position, new Vector2(0f, 0f));
	}

	@Override
	protected void defineBody(Vector2 position, Vector2 velocity) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, position, velocity);
		b2body.setGravityScale(GRAVITY_SCALE);
		spine = new MonoeyeSpine(this, UInfo.M2Tx(position.x));
		// agent sensor fixture
		B2DFactory.makeSensorBoxFixture(b2body, spine.createAgentSensor(), AS_CFCAT, AS_CFMASK,
				getBodySize().x, getBodySize().y);
		// create player sensor fixture that "hangs" down from the top of Monoeye and detects players to target
		B2DFactory.makeSensorBoxFixture(b2body, spine.createPlayerSensor(), CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, PLAYER_SENSOR_WIDTH, PLAYER_SENSOR_HEIGHT,
				new Vector2(0f, getBodySize().y/2f - PLAYER_SENSOR_HEIGHT/2f));
	}

	public MonoeyeSpine getSpine() {
		return spine;
	}
}
*/