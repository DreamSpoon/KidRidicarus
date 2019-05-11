package kidridicarus.game.Metroid.agent.other.metroiddoornexus;

import kidridicarus.agency.Agency.AgentHooks;
import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class MetroidDoorNexus extends CorpusAgent {
	private MetroidDoorNexusBrain brain;

	public MetroidDoorNexus(AgentHooks agentHooks, ObjectProperties properties) {
		super(agentHooks, properties);
		body = new MetroidDoorNexusBody(this, agentHooks.getWorld(), AP_Tool.getBounds(properties));
		brain = new MetroidDoorNexusBrain(this, agentHooks, (MetroidDoorNexusBody) body,
				properties.getString(CommonKV.Script.KEY_TARGET_LEFT, null),
				properties.getString(CommonKV.Script.KEY_TARGET_RIGHT, null));
		agentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((MetroidDoorNexusBody) body).processContactFrame());
				}
			});
		agentHooks.createAgentRemoveListener(this, new AgentRemoveCallback() {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}
}
