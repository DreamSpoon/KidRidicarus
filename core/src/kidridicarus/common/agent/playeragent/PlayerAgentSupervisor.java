package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agency;
import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;

public abstract class PlayerAgentSupervisor extends AgentSupervisor {
	private RoomBox currentRoom;
	private PowerupList nonCharPowerups;
	private Vector2 lastKnownViewCenter;

	public PlayerAgentSupervisor(Agency agency, Agent agent) {
		super(agency, agent);
		currentRoom = null;
		nonCharPowerups = new PowerupList();
		lastKnownViewCenter = null;
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

		// if current room is known the try to set last known view center
		RoomBox room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room != null)
			lastKnownViewCenter = room.getViewCenterForPos(playerAgent.getPosition(), lastKnownViewCenter);
	}

	private void roomChange(RoomBox newRoom) {
		if(newRoom != null)
			getAgency().getEar().changeAndStartMainMusic(newRoom.getRoommusic());
	}

	public Vector2 getViewCenter() {
		RoomBox room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room == null)
			return null;
		return room.getViewCenterForPos(playerAgent.getPosition(), lastKnownViewCenter);
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
