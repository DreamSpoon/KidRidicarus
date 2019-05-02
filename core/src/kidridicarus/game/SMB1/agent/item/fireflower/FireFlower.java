package kidridicarus.game.SMB1.agent.item.fireflower;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.info.SMB1_KV;

public class FireFlower extends CorpusAgent implements DisposableAgent {
	private FireFlowerBrain brain;
	private FireFlowerSprite sprite;

	public FireFlower(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new FireFlowerBody(this, agency.getWorld());
		brain = new FireFlowerBrain(this, (FireFlowerBody) body, AP_Tool.getCenter(properties));
		sprite = new FireFlowerSprite(this, agency.getAtlas(), ((FireFlowerBrain) brain).getSproutStartPos());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) {
				brain.processContactFrame(((FireFlowerBody) body).processContactFrame());
			}
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(delta)); }
			});
	}

	@Override
	public void disposeAgent() {
		dispose();
	}

	public static ObjectProperties makeAP(Vector2 position) {
		return AP_Tool.createPointAP(SMB1_KV.AgentClassAlias.VAL_FIREFLOWER, position);
	}
}