package kidridicarus.game.KidIcarus.agent.item.angelheart;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeartBrain.AngelHeartSize;
import kidridicarus.game.info.KidIcarusKV;

public class AngelHeart extends CorpusAgent implements DisposableAgent {
	private AngelHeartBrain brain;
	private AngelHeartSprite sprite;

	public AngelHeart(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		body = new AngelHeartBody(this, agency.getWorld(), AP_Tool.getCenter(agentProps));
		brain = new AngelHeartBrain(this, (AngelHeartBody) body,
				agentProps.get(KidIcarusKV.KEY_HEART_COUNT, 1, Integer.class));
		sprite = new AngelHeartSprite(agency.getAtlas(), AP_Tool.getCenter(agentProps), brain.getHeartSize());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					brain.processContactFrame(((AngelHeartBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(delta)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	@Override
	public void disposeAgent() {
		dispose();
	}

	public static ObjectProperties makeAP(Vector2 position, int heartCount) {
		if(!AngelHeartSize.isValidHeartCount(heartCount))
			throw new IllegalArgumentException("Unable to create Agent with heart count = " + heartCount);
		ObjectProperties props = AP_Tool.createPointAP(KidIcarusKV.AgentClassAlias.VAL_ANGEL_HEART, position);
		props.put(KidIcarusKV.KEY_HEART_COUNT, heartCount);
		return props;
	}
}
