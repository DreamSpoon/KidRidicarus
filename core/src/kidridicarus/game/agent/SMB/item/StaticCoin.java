package kidridicarus.game.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.optional.ItemAgent;
import kidridicarus.agency.tool.DrawOrder;
import kidridicarus.game.agent.SMB.player.Mario;
import kidridicarus.game.agent.body.SMB.item.StaticCoinBody;
import kidridicarus.game.agent.sprite.SMB.item.StaticCoinSprite;

public class StaticCoin extends Agent implements ItemAgent {
	private StaticCoinSprite coinSprite;
	private StaticCoinBody coinBody;

	public StaticCoin(Agency agency, AgentDef adef) {
		super(agency, adef);

		coinSprite = new StaticCoinSprite(agency.getAtlas(), adef.bounds.getCenter(new Vector2()));
		coinBody = new StaticCoinBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));

		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, DrawOrder.SPRITE_BOTTOM);
	}

	@Override
	public void update(float delta) {
		coinSprite.update(agency.getGlobalTimer());
	}

	@Override
	public void draw(Batch batch){
		coinSprite.draw(batch);
	}

	@Override
	public void use(Agent agent) {
		if(agent instanceof Mario) {
			((Mario) agent).giveCoin();
			agency.disposeAgent(this);
		}
	}

	@Override
	public Vector2 getPosition() {
		return coinBody.getPosition();
	}

	@Override
	public Rectangle getBounds() {
		return coinBody.getBounds();
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		coinBody.dispose();
	}
}