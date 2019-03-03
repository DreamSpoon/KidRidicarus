package kidridicarus.game.SMB.agent.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.agent.optional.PowerupGiveAgent;
import kidridicarus.game.SMB.agent.player.Mario;
import kidridicarus.game.SMB.agentbody.item.StaticCoinBody;
import kidridicarus.game.SMB.agentsprite.item.StaticCoinSprite;
import kidridicarus.game.info.GfxInfo;

public class StaticCoin extends Agent implements PowerupGiveAgent {
	private StaticCoinBody coinBody;
	private StaticCoinSprite coinSprite;

	public StaticCoin(Agency agency, ObjectProperties properties) {
		super(agency, properties);
		coinBody = new StaticCoinBody(this, agency.getWorld(), Agent.getStartPoint(properties));
		coinSprite = new StaticCoinSprite(agency.getAtlas(), coinBody.getPosition());
		agency.enableAgentUpdate(this);
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_BOTTOM);
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