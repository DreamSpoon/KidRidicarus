package kidridicarus.common.agent.levelendtrigger;

import kidridicarus.agency.Agency;
import kidridicarus.agency.Agent;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.agent.playeragent.PlayerAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class LevelEndTrigger extends CorpusAgent implements TriggerTakeAgent{
	private String nextLevelFilename;

	public LevelEndTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new LevelEndTriggerBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		nextLevelFilename = properties.getString(CommonKV.Level.VAL_NEXTLEVEL_FILENAME, "");
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
		for(PlayerAgent agent : ((LevelEndTriggerBody) body).getPlayerBeginContacts())
			agent.getSupervisor().startScript(new LevelEndScript(this, nextLevelFilename));
	}

	@Override
	public void onTakeTrigger() {
		Agent targetAgent = AP_Tool.getTargetAgent(this, agency);
		if(targetAgent instanceof TriggerTakeAgent)
			((TriggerTakeAgent) targetAgent).onTakeTrigger();
	}
}
