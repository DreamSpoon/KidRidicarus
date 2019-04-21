package kidridicarus.common.agent.characteragent;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.MotileBoundsAgent;
import kidridicarus.common.info.CommonInfo;

public abstract class RoleAgent extends MotileBoundsAgent {
	protected RoleAgentBody body;
	protected RoleAgentCharacter character;
	protected RoleAgentSprite sprite;

	public RoleAgent(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { character.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { sprite.processFrame(character.processFrame(body.processFrame(delta))); }
		});
	}
}
