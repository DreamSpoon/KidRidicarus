package kidridicarus.game.Metroid.agent.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentProperties;
import kidridicarus.common.agent.optional.ItemAgent;
import kidridicarus.common.agent.optional.ReceivePowerupAgent;
import kidridicarus.game.Metroid.agentbody.item.MaruMariBody;
import kidridicarus.game.Metroid.agentsprite.item.MaruMariSprite;
import kidridicarus.game.info.GfxInfo;
import kidridicarus.game.info.PowerupInfo.PowType;

public class MaruMari extends Agent implements ItemAgent {
	private MaruMariBody mmBody;
	private MaruMariSprite mmSprite;

	public MaruMari(Agency agency, AgentProperties agentProps) {
		super(agency, agentProps);
		mmBody = new MaruMariBody(this, agency.getWorld(), Agent.getStartPoint(agentProps));
		mmSprite = new MaruMariSprite(agency.getAtlas(), mmBody.getPosition());
		agency.setAgentDrawOrder(this, GfxInfo.LayerDrawOrder.SPRITE_MIDDLE);
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
		// if the other agent can receive this powerup then apply 
		if(agent instanceof ReceivePowerupAgent) {
			((ReceivePowerupAgent) agent).applyPowerup(PowType.MARUMARI);
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
	public Vector2 getVelocity() {
		return new Vector2(0f, 0f);
	}

	@Override
	public void dispose() {
		mmBody.dispose();
	}
}
