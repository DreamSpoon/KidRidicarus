package kidridicarus.agent.SMB.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentDef;
import kidridicarus.agent.Agent;
import kidridicarus.agent.SMB.player.Mario;
import kidridicarus.agent.bodies.SMB.item.StaticCoinBody;
import kidridicarus.agent.option.ItemAgent;
import kidridicarus.agent.sprites.SMB.item.StaticCoinSprite;
import kidridicarus.info.GameInfo.SpriteDrawOrder;

public class StaticCoin extends Agent implements ItemAgent, Disposable {
	private StaticCoinSprite coinSprite;
	private StaticCoinBody coinBody;

	public StaticCoin(Agency agency, AgentDef adef) {
		super(agency, adef);

		coinSprite = new StaticCoinSprite(agency.getEncapTexAtlas(), adef.bounds.getCenter(new Vector2()));
		coinBody = new StaticCoinBody(this, agency.getWorld(), adef.bounds.getCenter(new Vector2()));

		agency.enableAgentUpdate(this);
		agency.setAgentDrawLayer(this, SpriteDrawOrder.BOTTOM);
	}

	@Override
	public void update(float delta) {
		coinSprite.update(delta);
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
	public void dispose() {
		coinBody.dispose();
	}
}