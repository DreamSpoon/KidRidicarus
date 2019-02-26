package kidridicarus.agency.agent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.optional.PlayerAgent;

/*
 * 
 */
public abstract class AgentObserver {
	protected Agent playerAgent;

	public AgentObserver(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);
		this.playerAgent = agent;
	}

	public Vector2 getViewCenter() {
		Room room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room == null)
			return new Vector2(0f, 0f);
		return ((PlayerAgent) playerAgent).getCurrentRoom().getViewCenterForPos(playerAgent.getPosition());
	}

	public abstract void setStageHUD(Stage stageHUD);
	public abstract void drawHUD();
}
