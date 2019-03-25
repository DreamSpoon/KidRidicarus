package kidridicarus.game.agent.Metroid.NPC.skreeshot;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgTakeAgent;
import kidridicarus.common.info.CommonInfo;

public class SkreeShot extends Agent implements DisposableAgent {
	private static final float LIVE_TIME = 0.167f;
	private static final float GIVE_DAMAGE = 5f;

	private SkreeShotBody body;
	private SkreeShotSprite sprite;
	private float moveStateTimer;

	public SkreeShot(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		moveStateTimer = 0f;
		body = new SkreeShotBody(this, agency.getWorld(), Agent.getStartPoint(properties),
				Agent.getStartVelocity(properties));
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.CONTACT_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(float delta) { doContactUpdate(); }
		});
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new SkreeShotSprite(agency.getAtlas(), body.getPosition());
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	// apply damage to all contacting agents
	private void doContactUpdate() {
		for(ContactDmgTakeAgent agent : body.getContactDmgTakeAgents())
			agent.onTakeDamage(this, GIVE_DAMAGE, body.getPosition());
	}

	private void doUpdate(float delta) {
		if(moveStateTimer > LIVE_TIME) {
			agency.disposeAgent(this);
		}
		else {
			sprite.update(body.getPosition());
			moveStateTimer += delta;
		}
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(sprite);
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
