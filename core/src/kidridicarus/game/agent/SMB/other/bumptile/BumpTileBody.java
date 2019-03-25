package kidridicarus.game.agent.SMB.other.bumptile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.agency.agentcontact.CFBitSeq;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class BumpTileBody extends AgentBody {
	// all-in-one sensor
	private static final CFBitSeq MAINSENSOR_CFCAT = new CFBitSeq(CommonCF.Alias.BUMPABLE_BIT);
	private static final CFBitSeq MAINSENSOR_CFMASK = new CFBitSeq(CommonCF.Alias.COLLISIONMAP_BIT,
			CommonCF.Alias.AGENT_BIT);

	private BumpTileSpine spine;

	public BumpTileBody(World world, BumpTile parent, Rectangle bounds) {
		super(parent);
		setBodySize(bounds.width, bounds.height);
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		createMainBody(world, bounds.getCenter(new Vector2()));
		createSpineAndMainSensor(bounds);
	}

	private void createMainBody(World world, Vector2 position) {
		// should be a static body, but it needs to be dynamic so collision map contact sensor will function
		b2body = B2DFactory.makeDynamicBody(world, position);
		b2body.setGravityScale(0f);
	}

	private void createSpineAndMainSensor(Rectangle bounds) {
		spine = new BumpTileSpine(this);
		// sensor detects collision maps, and agents that can contact bump tiles
		B2DFactory.makeSensorBoxFixture(b2body, spine.createMainSensor(),
				MAINSENSOR_CFCAT, MAINSENSOR_CFMASK, bounds.width, bounds.height);
	}

	public BumpTileSpine getSpine() {
		return spine;
	}
}
