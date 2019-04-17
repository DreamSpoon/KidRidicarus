package kidridicarus.common.agent.briefstaticpowerup;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.staticpowerup.StaticPowerup;

public abstract class BriefStaticPowerup extends StaticPowerup {
	private float moveStateTimer;
	private float liveTime;

	public BriefStaticPowerup(Agency agency, ObjectProperties agentProps, float liveTime) {
		super(agency, agentProps);
		this.liveTime = liveTime;
		moveStateTimer = 0f;
	}

	@Override
	protected boolean doPowerupUpdate(float delta, boolean isPowUsed) {
		if(moveStateTimer > liveTime) {
			agency.removeAgent(this);
			return false;
		}
		moveStateTimer += delta;
		return true;
	}
}
