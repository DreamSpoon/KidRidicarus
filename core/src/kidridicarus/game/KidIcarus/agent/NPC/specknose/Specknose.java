package kidridicarus.game.KidIcarus.agent.NPC.specknose;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.MobileBoundsAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;

public class Specknose extends MobileBoundsAgent implements ContactDmgTakeAgent, DisposableAgent {
	private SpecknoseBody body;
	private SpecknoseBrain brain;
	private SpecknoseSprite sprite;

	public Specknose(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		body = new SpecknoseBody(
				this, agency.getWorld(), AP_Tool.getCenter(properties), AP_Tool.getVelocity(properties));
		brain = new SpecknoseBrain(this, body);
		sprite = new SpecknoseSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { brain.processContactFrame(body.processContactFrame()); }
			});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { sprite.processFrame(brain.processFrame(delta)); }
			});
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye eye) { eye.draw(sprite); }
			});
	}

	// assume any amount of damage kills, for now...
	@Override
	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return ((SpecknoseBrain) brain).onTakeDamage(agent, amount, dmgOrigin);
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
	protected Vector2 getVelocity() {
		return body.getVelocity();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
