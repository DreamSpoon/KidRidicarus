package kidridicarus.common.agent.playeragent;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.agency.agent.AgentSupervisor;
import kidridicarus.common.agent.roombox.RoomBox;
import kidridicarus.common.powerup.Powerup;
import kidridicarus.common.powerup.PowerupList;

public abstract class PlayerAgentSupervisor extends AgentSupervisor {
	private RoomBox currentRoom;
	private PowerupList nonCharPowerups;
	private Vector2 lastKnownViewCenter;

	public PlayerAgentSupervisor(Agent agent) {
		super(agent);
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
		// Reset the view center, so that view does not "over-scroll" if player is teleported in a one way
		// scrolling room (e.g. using doors in Kid Icarus level 1-1).
		lastKnownViewCenter = null;
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
