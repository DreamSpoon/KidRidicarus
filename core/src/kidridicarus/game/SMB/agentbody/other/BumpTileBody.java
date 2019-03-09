package kidridicarus.game.SMB.agentbody.other;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.agentbody.sensor.AgentContactHoldSensor;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.game.SMB.agent.other.BumpTile;
import kidridicarus.game.SMB.agentbody.BumpableTileBody;

public class BumpTileBody extends AgentBody implements BumpableTileBody {
	// agent sensor (basically)
	private static final CFBitSeq AS_CFCAT = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq AS_CFMASK = new CFBitSeq(CommonCF.Alias.AGENT_BIT);
	// collision map sensor
	private static final CFBitSeq SOLIDSENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq SOLID_SENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.COLLISIONMAP_BIT);

	private BumpTile parent;
	private AgentContactHoldSensor collisionMapSensor;

	public BumpTileBody(World world, BumpTile parent, Rectangle bounds) {
		this.parent = parent;
		setBodySize(bounds.width, bounds.height);
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		createMainBody(world, bounds);
		createCollisionMapSensor(world, bounds);
	}

	// a player agent can contact this fixture for bump tiles
	private void createMainBody(World world, Rectangle bounds) {
		BodyDef bdef = new BodyDef();
		// should be a static body, but it needs to be dynamic so collision map contact sensor will function
		bdef.type = BodyDef.BodyType.DynamicBody;
		bdef.gravityScale = 0f;
		bdef.position.set(bounds.getCenter(new Vector2()));
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		b2body = B2DFactory.makeSpecialBoxBody(world, bdef, fdef, this, AS_CFCAT, AS_CFMASK,
				bounds.width, bounds.height);
	}

	// only the collision map can contact this fixture
	// TODO Combine the two fixtures?
	private void createCollisionMapSensor(World world, Rectangle bounds) {
		FixtureDef fdef = new FixtureDef();
		fdef.isSensor = true;
		collisionMapSensor = new AgentContactHoldSensor(this);
		B2DFactory.makeBoxFixture(b2body, fdef, collisionMapSensor, SOLIDSENSOR_CFCAT, SOLID_SENSOR_CFMASK,
				bounds.width, bounds.height);
	}

	@Override
	public Agent getParent() {
		return parent;
	}

	@Override
	public void onBumpTile(Agent bumpingAgent) {
		parent.onBumpTile(bumpingAgent);
	}

	public <T> T getFirstContactByClass(Class<T> cls) {
		return collisionMapSensor.getFirstContactByClass(cls);
	}
}
