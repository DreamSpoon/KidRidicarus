package kidridicarus.game.agent.Metroid.NPC.skree;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.ContactDmgGiveAgent;
import kidridicarus.common.info.CommonInfo;

public class SkreeExp extends Agent implements ContactDmgGiveAgent, DisposableAgent {
	private static final float LIVE_TIME = 0.167f;

	private SkreeExpBody seBody;
	private SkreeExpSprite seSprite;
	private float stateTimer;

	public SkreeExp(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		seBody = new SkreeExpBody(this, agency.getWorld(), Agent.getStartPoint(properties),
				Agent.getStartVelocity(properties));
		seSprite = new SkreeExpSprite(agency.getAtlas(), seBody.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM, new AgentDrawListener() {
			@Override
			public void draw(AgencyDrawBatch batch) { doDraw(batch); }
		});
	}

	private void doUpdate(float delta) {
		if(stateTimer > LIVE_TIME) {
			agency.disposeAgent(this);
		}
		else {
			seSprite.update(seBody.getPosition());
			stateTimer += delta;
		}
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(seSprite);
	}

	@Override
	public Vector2 getPosition() {
		return seBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return seBody.getBounds();
	}

	@Override
	public boolean isContactDamage() {
		return true;
	}

	@Override
	public void disposeAgent() {
		seBody.dispose();
	}
}
