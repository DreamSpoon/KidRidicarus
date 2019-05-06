package kidridicarus.game.Metroid.agent.player.samusshot;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.FrameTime;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentRemoveListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.CorpusAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.Metroid.agent.player.samus.Samus;
import kidridicarus.game.info.MetroidKV;

public class SamusShot extends CorpusAgent {
	private SamusShotBrain brain;
	private SamusShotSprite sprite;

	public SamusShot(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new SamusShotBody(this, agency.getWorld(), AP_Tool.getCenter(properties),
				AP_Tool.getVelocity(properties));
		brain = new SamusShotBrain(this, (SamusShotBody) body,
				properties.get(CommonKV.KEY_PARENT_AGENT, null, Samus.class),
				properties.containsKV(CommonKV.Spawn.KEY_EXPIRE, true));
		sprite = new SamusShotSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) {
					brain.processContactFrame(((SamusShotBody) body).processContactFrame());
				}
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(FrameTime frameTime) { sprite.processFrame(brain.processFrame(frameTime)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
		agency.addAgentRemoveListener(new AgentRemoveListener(this, this) {
				@Override
				public void preRemoveAgent() { dispose(); }
			});
	}

	// make the AgentProperties (AP) for this class of Agent
	public static ObjectProperties makeAP(Samus parentAgent, Vector2 position, Vector2 velocity,
			boolean isExpireImmediately) {
		ObjectProperties props = AP_Tool.createPointAP(MetroidKV.AgentClassAlias.VAL_SAMUS_SHOT,
				position, velocity);
		props.put(CommonKV.KEY_PARENT_AGENT, parentAgent);
		props.put(CommonKV.Spawn.KEY_EXPIRE, isExpireImmediately);
		return props;
	}
}
