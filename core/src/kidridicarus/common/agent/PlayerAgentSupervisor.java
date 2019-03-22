package kidridicarus.common.agent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;

public abstract class PlayerAgentSupervisor extends AgentSupervisor {
	private RoomBox currentRoom;
	private Vector2 lastViewCenter;
	private PowerupList nonCharPowerups;

	public abstract void setStageHUD(Stage stageHUD);
	public abstract void drawHUD();

	public PlayerAgentSupervisor(Agency agency, Agent agent) {
		super(agency, agent);
		currentRoom = null;
		lastViewCenter = new Vector2(0f, 0f);
		nonCharPowerups = new PowerupList();
	}

	@Override
	public void postUpdateAgency() {
		super.postUpdateAgency();

		// check if player changed room, and if so, did the room music change?
		RoomBox nextRoom = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(currentRoom != nextRoom) {
			roomChange(nextRoom);
			currentRoom = nextRoom;
		}
	}

	public void roomChange(RoomBox newRoom) {
		if(newRoom != null)
			agency.getEar().onChangeAndStartMainMusic(newRoom.getRoommusic());
	}

	/*
	 * Check current room to get view center, and retain last known view center if room becomes null.
	 */
	public Vector2 getViewCenter() {
		RoomBox room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room == null)
			return lastViewCenter;
		lastViewCenter.set(((PlayerAgent) playerAgent).getCurrentRoom().getViewCenterForPos(
				playerAgent.getPosition()));
		return lastViewCenter;
	}

	public void receiveNonCharPowerup(Powerup pow) {
		nonCharPowerups.add(pow);
	}

	public PowerupList getNonCharPowerups() {
		return nonCharPowerups;
	}

	public void clearNonCharPowerups() {
		nonCharPowerups.clear();
	}
}
