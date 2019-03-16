package kidridicarus.game.agent.Metroid.other.deathpop;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;

public class DeathPop extends Agent {
	private static final float POP_TIME = 3f/60f;

	private DeathPopSprite dpSprite;
	private float stateTimer;
	private Vector2 position;

	public DeathPop(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		position = Agent.getStartPoint(properties);
		dpSprite = new DeathPopSprite(agency.getAtlas(), position);
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		if(stateTimer > POP_TIME)
			agency.disposeAgent(this);
		dpSprite.update(delta);
		stateTimer += delta;
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(dpSprite);
	}

	@Override
	public Vector2 getPosition() {
		return position;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(position.x, position.y, 0f, 0f);
	}
}
