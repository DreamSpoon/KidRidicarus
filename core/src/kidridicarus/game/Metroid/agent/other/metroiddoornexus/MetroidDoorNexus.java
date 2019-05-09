package kidridicarus.game.Metroid.agent.other.metroiddoornexus;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class MetroidDoorNexus extends CorpusAgent {
	private MetroidDoorNexusBrain brain;

	public MetroidDoorNexus(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new MetroidDoorNexusBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		brain = new MetroidDoorNexusBrain(this, (MetroidDoorNexusBody) body,
				properties.getString(CommonKV.Script.KEY_TARGET_LEFT, null),
				properties.getString(CommonKV.Script.KEY_TARGET_RIGHT, null));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((MetroidDoorNexusBody) body).processContactFrame());
				}
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}
}
