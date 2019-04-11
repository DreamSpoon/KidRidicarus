package kidridicarus.game.agent.SMB1.item.fireflower;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.BasicAgentSpine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class FireFlowerBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private BasicAgentSpine spine;

	public FireFlowerBody(FireFlower parent, World world, Vector2 position) {
		super(parent, world);
		defineBody(new Rectangle(position.x, position.y, 0f, 0f));
	}

	@Override
	protected void defineBody(Rectangle bounds) {
		// dispose the old body if it exists	
		if(b2body != null)	
			world.destroyBody(b2body);

		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, bounds.getCenter(new Vector2()));
		spine = new BasicAgentSpine(this);
		// agent sensor fixture
		B2DFactory.makeBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.POWERUP_CFCAT, CommonCF.POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public BasicAgentSpine getSpine() {
		return spine;
	}
}
