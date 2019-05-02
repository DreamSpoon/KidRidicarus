package kidridicarus.game.Metroid.agent.other.metroiddoornexus;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

public class MetroidDoorNexus extends CorpusAgent implements DisposableAgent {
	private MetroidDoorNexusBrain brain;

	public MetroidDoorNexus(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new MetroidDoorNexusBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		brain = new MetroidDoorNexusBrain(this, (MetroidDoorNexusBody) body,
				getProperty(CommonKV.Script.KEY_TARGET_LEFT, null, String.class),
				getProperty(CommonKV.Script.KEY_TARGET_RIGHT, null, String.class));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					brain.processContactFrame(((MetroidDoorNexusBody) body).processContactFrame());
				}
			});
	}

	@Override
	public void disposeAgent() {
		dispose();
	}
}
