package kidridicarus.common.agent.agentspawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;

import kidridicarus.agency.agentbody.CFBitSeq;
import kidridicarus.common.agent.followbox.FollowBoxBody;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.agentsensor.OneWayContactSensor;
import kidridicarus.common.info.CommonCF;

public class AgentSpawnTriggerBody extends FollowBoxBody {
	private static final CFBitSeq CFCAT_BITS = new CFBitSeq(CommonCF.Alias.SPAWNTRIGGER_BIT);
	private static final CFBitSeq CFMASK_BITS = new CFBitSeq(true);

	private OneWayContactSensor beginContactSensor;
	private OneWayContactSensor endContactSensor;

	public AgentSpawnTriggerBody(AgentSpawnTrigger parent, World world, Rectangle bounds) {
		super(parent, world, bounds, true);
		beginContactSensor = new OneWayContactSensor(this, true);
		endContactSensor = new OneWayContactSensor(this, false);
		beginContactSensor.chainTo(endContactSensor);
	}

	public SpawnTriggerFrameInput processFrame() {
		return new SpawnTriggerFrameInput(beginContactSensor.getOnlyAndResetContacts(EnableTakeAgent.class),
				endContactSensor.getOnlyAndResetContacts(EnableTakeAgent.class));
	}

	@Override
	protected CFBitSeq getCatBits() {
		return CFCAT_BITS;
	}

	@Override
	protected CFBitSeq getMaskBits() {
		return CFMASK_BITS;
	}

	@Override
	protected Object getSensorBoxUserData() {
		return beginContactSensor;
	}
}
