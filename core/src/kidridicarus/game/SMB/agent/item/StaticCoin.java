package kidridicarus.game.SMB.agent.item;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgentUpdateListener;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.tool.AgencyDrawBatch;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agentbody.item.StaticCoinBody;
import kidridicarus.game.SMB.agentsprite.item.StaticCoinSprite;

public class StaticCoin extends Agent implements DrawableAgent, PowerupGiveAgent,
		DisposableAgent {
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
		agency.setAgentDrawOrder(this, CommonInfo.LayerDrawOrder.SPRITE_BOTTOM);
	}

	private void doUpdate(float delta) {
		coinSprite.update(agency.getGlobalTimer());
	}

	@Override
	public void draw(AgencyDrawBatch batch){
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