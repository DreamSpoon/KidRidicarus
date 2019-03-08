package kidridicarus.game.Metroid.agent.NPC;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyDrawBatch;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.GfxInfo;
import kidridicarus.game.Metroid.agentsprite.NPC.DeathPopSprite;

public class DeathPop extends Agent implements UpdatableAgent, DrawableAgent, DisposableAgent {
	private static final float POP_TIME = 3f/60f;

	private DeathPopSprite dpSprite;
	private float stateTimer;
	private Vector2 position;

	public DeathPop(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		position = Agent.getStartPoint(properties);
		dpSprite = new DeathPopSprite(agency.getAtlas(), position);
		agency.setAgentUpdateOrder(this, CommonInfo.AgentUpdateOrder.UPDATE);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
	}

	@Override
	public void update(float delta) {
		if(stateTimer > POP_TIME)
			agency.disposeAgent(this);
		dpSprite.update(delta);
		stateTimer += delta;
	}

	@Override
	public void draw(AgencyDrawBatch batch) {
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

	@Override
	public void disposeAgent() {
	}
}
