package kidridicarus.game.KidIcarus.agent.NPC.nettler;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agentproperties.ObjectProperties;
import kidridicarus.agency.tool.Eye;
import kidridicarus.common.agent.general.PlacedBoundsAgent;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.tool.AP_Tool;
import kidridicarus.game.SMB1.agent.BumpTakeAgent;

public class Nettler extends PlacedBoundsAgent implements ContactDmgTakeAgent, BumpTakeAgent, DisposableAgent {
	private NettlerBody body;
	private NettlerCharacter character;
	private NettlerSprite sprite;

	public Nettler(Agency agency, ObjectProperties properties) {
		super(agency, properties);

		body = new NettlerBody(
				this, agency.getWorld(), AP_Tool.getCenter(properties), AP_Tool.getVelocity(properties));
		character = new NettlerCharacter(this, body);
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { character.processContactFrame(body.processContactFrame()); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.UpdateOrder.MOVE_UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) {
					sprite.processFrame(character.processFrame(body.processFrame(delta)));
				}
				
			});
		sprite = new NettlerSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.DrawOrder.SPRITE_TOPFRONT, new AgentDrawListener() {
				@Override
				public void draw(Eye adBatch) { adBatch.draw(sprite); }
			});
	}

	@Override
	public void onTakeBump(Agent agent) {
		character.onTakeBump(agent);
	}

	public boolean onTakeDamage(Agent agent, float amount, Vector2 dmgOrigin) {
		return character.onTakeDamage(agent, amount, dmgOrigin);
	}

	@Override
	public Vector2 getPosition() {
		return body.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return body.getBounds();
	}

	@Override
	public void disposeAgent() {
		body.dispose();
	}
}
