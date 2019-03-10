package kidridicarus.game.Metroid.agent.item;

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
import kidridicarus.common.agent.optional.PowerupTakeAgent;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.game.Metroid.agentbody.item.MaruMariBody;
import kidridicarus.game.Metroid.agentsprite.item.MaruMariSprite;
import kidridicarus.game.info.PowerupInfo.PowType;

public class MaruMari extends Agent implements PowerupGiveAgent, DisposableAgent {
	private MaruMariBody mmBody;
	private MaruMariSprite mmSprite;

	public MaruMari(Agency agency, ObjectProperties agentProps) {
		super(agency, agentProps);
		mmBody = new MaruMariBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		mmSprite = new MaruMariSprite(agency.getAtlas(), mmBody.getPosition());
		agency.addAgentUpdateListener(this, CommonInfo.AgentUpdateOrder.UPDATE, new AgentUpdateListener() {
				@Override
				public void update(float delta) { doUpdate(delta); }
			});
		agency.addAgentDrawListener(this, CommonInfo.LayerDrawOrder.SPRITE_MIDDLE, new AgentDrawListener() {
				@Override
				public void draw(AgencyDrawBatch batch) { doDraw(batch); }
			});
	}

	private void doUpdate(float delta) {
		mmSprite.update(delta, mmBody.getPosition());
	}

	public void doDraw(AgencyDrawBatch batch) {
		batch.draw(mmSprite);
	}

	@Override
	public void use(Agent agent) {
		// if the other agent can receive this powerup then apply 
		if(agent instanceof PowerupTakeAgent) {
			((PowerupTakeAgent) agent).applyPowerup(PowType.MARUMARI);
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
	public void disposeAgent() {
		mmBody.dispose();
	}
}
