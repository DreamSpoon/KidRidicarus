package kidridicarus.game.agent.KidIcarus.NPC.smallpoof;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;

public class SmallPoof extends Agent {
	private static final float POOF_TIME = 2/5f;

	private Vector2 position;
	private SmallPoofSprite sprite;
	private float stateTimer;

	public SmallPoof(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		position = Agent.getStartPoint(properties);
		stateTimer = 0f;
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		sprite = new SmallPoofSprite(agency.getAtlas(), position);
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		if(stateTimer > POOF_TIME)
			agency.removeAgent(this);
		sprite.update(delta);
		stateTimer += delta;
	}

	private void doDraw(AgencyDrawBatch batch) {
		batch.draw(sprite);
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
