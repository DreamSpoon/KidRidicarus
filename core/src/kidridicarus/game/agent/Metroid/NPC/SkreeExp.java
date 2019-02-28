package kidridicarus.game.agent.Metroid.NPC;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDef;
import kidridicarus.common.agent.optional.ContactDmgAgent;
import kidridicarus.game.agentbody.Metroid.NPC.SkreeExpBody;
import kidridicarus.game.agentsprite.Metroid.NPC.SkreeExpSprite;
import kidridicarus.game.info.GfxInfo;

public class SkreeExp extends Agent implements ContactDmgAgent {
	private static final float LIVE_TIME = 0.167f;

	private SkreeExpBody seBody;
	private SkreeExpSprite seSprite;
	private float stateTimer;

	public SkreeExp(Agency agency, AgentDef adef) {
		super(agency, adef);

		stateTimer = 0f;

		seBody = new SkreeExpBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()), adef.velocity);
		seSprite = new SkreeExpSprite(agency.getAtlas(), seBody.getPosition());

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_BOTTOM);
	}

	@Override
	public void update(float delta) {
		if(stateTimer > LIVE_TIME) {
			agency.disposeAgent(this);
		}
		else {
			seSprite.update(seBody.getPosition());
			stateTimer += delta;
		}
	}

	@Override
	public void draw(Batch batch) {
		seSprite.draw(batch);
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
	public Vector2 getVelocity() {
		return seBody.getVelocity();
	}

	@Override
	public void dispose() {
		seBody.dispose();
	}
}
