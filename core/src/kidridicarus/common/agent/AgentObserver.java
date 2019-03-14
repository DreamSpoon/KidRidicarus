package kidridicarus.common.agent;

import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public abstract class AgentObserver {
	protected Agent playerAgent;
	private RoomBox currentRoom;

	public abstract void setStageHUD(Stage stageHUD);
	public abstract void drawHUD();

	public AgentObserver(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);
		this.playerAgent = agent;
		currentRoom = null;
	}

	/*
	 * Check for room changes; which may lead to view changes, music changes, etc.
	 */
	public void postUpdateAgency() {
		// check if player changed room, and if so, did the room music change?
		RoomBox nextRoom = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(currentRoom != nextRoom) {
			roomChange(nextRoom);
			currentRoom = nextRoom;
		}
	}

	public abstract void roomChange(RoomBox newRoom);
	public abstract void startSinglePlayMusic(String musicName);
	public abstract void stopAllMusic();
}
