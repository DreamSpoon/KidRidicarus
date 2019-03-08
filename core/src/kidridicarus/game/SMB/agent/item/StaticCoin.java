package kidridicarus.game.SMB.agent.item;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.AgencyDrawBatch;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.DisposableAgent;
import kidridicarus.agency.agent.DrawableAgent;
import kidridicarus.agency.agent.UpdatableAgent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.GfxInfo;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agentbody.item.StaticCoinBody;
import kidridicarus.game.SMB.agentsprite.item.StaticCoinSprite;

public class StaticCoin extends Agent implements UpdatableAgent, DrawableAgent, PowerupGiveAgent,
		DisposableAgent {
	private StaticCoinBody coinBody;
	private StaticCoinSprite coinSprite;

	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		coinBody = new StaticCoinBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		coinSprite = new StaticCoinSprite(agency.getAtlas(), coinBody.getPosition());
		agency.setAgentUpdateOrder(this, CommonInfo.AgentUpdateOrder.UPDATE);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_BOTTOM);
	}

	@Override
	public void update(float delta) {
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