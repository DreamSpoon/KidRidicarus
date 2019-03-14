package kidridicarus.game.agent.SMB.other.levelendtrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.tool.B2DFactory;

public class LevelEndTriggerBody extends AgentBody {
	private LevelEndTrigger parent;

	public LevelEndTriggerBody(LevelEndTrigger parent, World world, Rectangle bounds) {
		this.parent = parent;
		defineBody(world, bounds);
	}

	private void defineBody(World world, Rectangle bounds) {
		setBodySize(bounds.width, bounds.height);
		b2body = B2DFactory.makeBoxBody(world, BodyType.StaticBody, this, CommonCF.AGENT_SENSOR_CFCAT,
				CommonCF.AGENT_SENSOR_CFMASK, bounds);
	}

	@Override
	public Agent getParent() {
		return parent;
	}
}
