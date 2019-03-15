package kidridicarus.game.agent.SMB.item.staticcoin;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.agentspine.PowerupSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class StaticCoinBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(16f);
	private static final float BODY_HEIGHT = UInfo.P2M(16f);

	private StaticCoin parent;
	private PowerupSpine spine;

	public StaticCoinBody(StaticCoin parent, World world, Vector2 position) {
		this.parent = parent;
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		createBody(world, position);
		createFixtures();
	}
	
	private void createBody(World world, Vector2 position) {
		BodyDef bdef;
		bdef = new BodyDef();
		bdef.position.set(position.x, position.y);
		bdef.type = BodyDef.BodyType.StaticBody;
		b2body = world.createBody(bdef);

		spine = new PowerupSpine(this);
	}

	private void createFixtures() {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		B2DFactory.makeBoxFixture(b2body, fdef, spine.createAgentSensor(),
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public PowerupSpine getSpine() {
		return spine;
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
