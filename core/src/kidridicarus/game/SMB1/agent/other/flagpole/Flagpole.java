package kidridicarus.game.SMB1.agent.other.flagpole;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.TriggerTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

public class Flagpole extends PlacedBoundsAgent implements TriggerTakeAgent, DisposableAgent {
	private FlagpoleBody body;
	private FlagpoleBrain brain;
	private PoleFlagSprite sprite;

	public Flagpole(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new FlagpoleBody(this, agency.getWorld(), AP_Tool.getBounds(properties));
		brain = new FlagpoleBrain(this, body);
		sprite = new PoleFlagSprite(agency.getAtlas(), brain.getFlagPosAtTop());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
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
	public void onTakeTrigger() {
		((FlagpoleBrain) brain).onTakeTrigger();
	}

	@Override
	protected Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	protected Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
