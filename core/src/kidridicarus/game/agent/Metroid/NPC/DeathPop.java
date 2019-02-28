package kidridicarus.game.agent.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.game.agentsprite.Metroid.NPC.DeathPopSprite;
import kidridicarus.game.info.GfxInfo;

public class DeathPop extends Agent {
	private static final float POP_TIME = 3f/60f;

	private DeathPopSprite dpSprite;
	private Rectangle bounds;
	private float stateTimer;

	public DeathPop(Agency agency, AgentDef adef) {
		super(agency, adef);
		bounds = adef.bounds;
		stateTimer = 0f;
		dpSprite = new DeathPopSprite(agency.getAtlas(), bounds.getCenter(new Vector2()));
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
		return bounds.getCenter(new Vector2());
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
	}
}
