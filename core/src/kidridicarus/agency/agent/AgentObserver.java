package kidridicarus.agency.agent;

import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.optional.PlayerAgent;

public abstract class AgentObserver {
	protected Agent playerAgent;
	private Room currentRoom;

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
		Room nextRoom = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(currentRoom != nextRoom) {
			roomChange(nextRoom);
			currentRoom = nextRoom;
		}
	}

	public abstract void roomChange(Room newRoom);
	public abstract void startSinglePlayMusic(String musicName);
	public abstract void stopAllMusic();
}
