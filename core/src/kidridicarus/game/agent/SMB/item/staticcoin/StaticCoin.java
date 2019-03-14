package kidridicarus.game.agent.SMB.item.staticcoin;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentDrawListener;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.agent.SMB.player.mario.Mario;

public class StaticCoin extends Agent implements PowerupGiveAgent, DisposableAgent {
	private StaticCoinBody coinBody;
	private StaticCoinSprite coinSprite;

	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		coinBody = new StaticCoinBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		coinSprite = new StaticCoinSprite(agency.getAtlas(), coinBody.getPosition());
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
		coinSprite.update(agency.getGlobalTimer());
	}

	public void doDraw(AgencyDrawBatch batch){
		batch.draw(coinSprite);
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
	public void disposeAgent() {
		coinBody.dispose();
	}
}