package kidridicarus.common.agent.levelendtrigger;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class LevelEndTrigger extends CorpusAgent implements TriggerTakeAgent{
	public LevelEndTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new LevelEndTriggerBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) { doContactUpdate(); }
		});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	private void doContactUpdate() {
		for(PlayerAgent agent : ((LevelEndTriggerBody) body).getPlayerBeginContacts()) {
			agent.getSupervisor().startScript(
					new LevelEndScript(this, getProperty(CommonKV.Level.VAL_NEXTLEVEL_FILENAME, "", String.class)));
		}
	}

	@Override
	public void onTakeTrigger() {
		String targetNameStr = getProperty(CommonKV.Script.KEY_TARGET_NAME, null, String.class);
		if(targetNameStr == null)
			return;
		Agent agent = agency.getFirstAgentByProperties(
				new String[] { CommonKV.Script.KEY_NAME }, new String[] { targetNameStr });
		if(agent instanceof TriggerTakeAgent)
			((TriggerTakeAgent) agent).onTakeTrigger();
	}
}
