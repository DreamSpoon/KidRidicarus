package kidridicarus.game.SMB1.agent.other.bumptile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

class BumpTileBody extends AgentBody {
	private static final CFBitSeq MAINSENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq MAINSENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.SOLID_MAP_BIT,
			CommonCF.Alias.AGENT_BIT);

	private BasicAgentSpine spine;

	BumpTileBody(World world, BumpTile parent, Rectangle bounds) {
		super(parent, world);
		defineBody(bounds);
	}

	// velocity is ignored
	@Override
	protected void defineBody(Rectangle bounds, Vector2 velocity) {
		// dispose the old body if it exists
		if(b2body != null)
			world.destroyBody(b2body);

		setBoundsSize(bounds.width, bounds.height);
		// should be a static body, but it needs to be dynamic so solid tile map contact sensor will function
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()));
		b2body.setGravityScale(0f);
		spine = new BasicAgentSpine(this);
		// sensor contacts solid tile maps, and Agents that can contact bump tiles
		B2DFactory.makeSensorBoxFixture(b2body, MAINSENSOR_CFCAT, MAINSENSOR_CFMASK, spine.createAgentSensor(),
				bounds.width, bounds.height);
	}

	BasicAgentSpine getSpine() {
		return spine;
	}
}
