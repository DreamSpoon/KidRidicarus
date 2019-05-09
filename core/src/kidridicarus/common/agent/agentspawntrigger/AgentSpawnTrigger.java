package kidridicarus.common.agent.agentspawntrigger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.followbox.FollowBox;
import kidridicarus.common.agent.optional.EnableTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;

// this class does not implement DisposableAgent because this is a sub-Agent related to player Agents
public class AgentSpawnTrigger extends FollowBox {
	public AgentSpawnTrigger(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new AgentSpawnTriggerBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					processFrame(((AgentSpawnTriggerBody) body).processFrame());
				}
			});
	}

	private void processFrame(SpawnTriggerFrameInput frameInput) {
		for(EnableTakeAgent agent : frameInput.beginContacts)
			agent.onTakeEnable(true);
		for(EnableTakeAgent agent : frameInput.endContacts)
			agent.onTakeEnable(false);
	}

	public static ObjectProperties makeAP(Vector2 position, float width, float height) {
		return AP_Tool.createRectangleAP(CommonKV.AgentClassAlias.VAL_AGENTSPAWN_TRIGGER,
				new Rectangle(position.x - width/2f, position.y - height/2f, width, height));
	}
}
