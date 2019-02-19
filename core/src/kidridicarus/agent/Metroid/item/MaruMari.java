package kidridicarus.agent.Metroid.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.PlayerAgent;
import kidridicarus.agent.body.Metroid.item.MaruMariBody;
import kidridicarus.agent.optional.ItemAgent;
import kidridicarus.agent.sprite.Metroid.item.MaruMariSprite;
import kidridicarus.info.GameInfo.SpriteDrawOrder;
import kidridicarus.info.PowerupInfo.PowType;

public class MaruMari extends Agent implements ItemAgent {
	private MaruMariBody mmBody;
	private MaruMariSprite mmSprite;

	public MaruMari(Agency agency, AgentDef adef) {
		super(agency, adef);

		mmBody = new MaruMariBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));
		mmSprite = new MaruMariSprite(agency.getAtlas(), mmBody.getPosition());
		agency.setAgentDrawOrder(this, SpriteDrawOrder.MIDDLE);
		agency.enableAgentUpdate(this);
	}

	@Override
	public void update(float delta) {
		mmSprite.update(delta, mmBody.getPosition());
	}

	@Override
	public void draw(Batch batch) {
		mmSprite.draw(batch);
	}

	@Override
	public void use(Agent agent) {
		Object d = agent;
		if(d instanceof PlayerAgent) {
			((PlayerAgent) d).applyPowerup(PowType.MARUMARI);
			agency.disposeAgent(this);
		}
	}

	@Override
	public Vector2 getPosition() {
		return mmBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return mmBody.getBounds();
	}

	@Override
	public void dispose() {
		mmBody.dispose();
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}
}
