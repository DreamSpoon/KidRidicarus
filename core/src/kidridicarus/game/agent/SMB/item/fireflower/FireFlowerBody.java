package kidridicarus.game.agent.SMB.item.fireflower;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.AgentBody;
import kidridicarus.common.agentspine.NPC_Spine;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.tool.B2DFactory;

public class FireFlowerBody extends AgentBody {
	private static final float BODY_WIDTH = UInfo.P2M(14f);
	private static final float BODY_HEIGHT = UInfo.P2M(12f);

	private NPC_Spine spine;

	public FireFlowerBody(FireFlower parent, World world, Vector2 position) {
		super(parent);
		defineBody(world, position);
	}

	private void defineBody(World world, Vector2 position) {
		setBodySize(BODY_WIDTH, BODY_HEIGHT);
		b2body = B2DFactory.makeDynamicBody(world, position);
		spine = new NPC_Spine(this);
		B2DFactory.makeBoxFixture(b2body, spine.createAgentSensor(),
				CommonCF.SOLID_POWERUP_CFCAT, CommonCF.SOLID_POWERUP_CFMASK, BODY_WIDTH, BODY_HEIGHT);
	}

	public NPC_Spine getSpine() {
		return spine;
	}
}
