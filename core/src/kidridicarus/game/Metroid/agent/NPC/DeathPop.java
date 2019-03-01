package kidridicarus.game.Metroid.agent.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentProperties;
import kidridicarus.game.Metroid.agentsprite.NPC.DeathPopSprite;
import kidridicarus.game.info.GfxInfo;

public class DeathPop extends Agent {
	private static final float POP_TIME = 3f/60f;

	private DeathPopSprite dpSprite;
	private float stateTimer;
	private Vector2 position;

	public DeathPop(Agency agency, AgentProperties properties) {
		super(agency, properties);
		stateTimer = 0f;
		position = Agent.getStartPoint(properties);
		dpSprite = new DeathPopSprite(agency.getAtlas(), position);
		agency.enableAgentUpdate(this);
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
	public void draw(Batch batch) {
		dpSprite.draw(batch);
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
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
	}
}
