package kidridicarus.agency.agent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.optional.PlayerAgent;

/*
 * TODO:
 * Observer needs an observer listener. The listener will listen for one off events like single play music,
 * and receive them immediately when the event occurs. The observer can also return info about current
 * music playing (and in future, keep a timer to know if the single play music has finished - so it could
 * return info on single play music as well).
 * 
 *   Route all observables through through agent observer, not just music.
 *     e.g. route sound through oberserver first, then maybe the graphics?
 *   This would help with the server/client code separation. 
 */
public abstract class AgentObserver {
	public interface AgentObserverListener {
		public void startRoomMusic(String musicName);
		public void startSinglePlayMusic(String musicName);
		public void stopAllMusic();
	}
	private AgentObserverListener listener; 

	protected Agent playerAgent;

	public abstract void setStageHUD(Stage stageHUD);
	public abstract void drawHUD();

	public AgentObserver(Agent agent) {
		if(!(agent instanceof PlayerAgent))
			throw new IllegalArgumentException("agent is not instanceof PlayerAgent: " + agent);
		this.playerAgent = agent;
	}

	/*
	 * Set listener to null to stop listening to this observer.
	 */
	public void setListener(AgentObserverListener listener) {
		this.listener = listener;
	}

	public Vector2 getViewCenter() {
		Room room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room == null)
			return new Vector2(0f, 0f);
		return ((PlayerAgent) playerAgent).getCurrentRoom().getViewCenterForPos(playerAgent.getPosition());
	}

	public void startRoomMusic(String musicName) {
		if(listener != null)
			listener.startRoomMusic(musicName);
	}

	public void startSinglePlayMusic(String musicName) {
		if(listener != null)
			listener.startSinglePlayMusic(musicName);
	}

	public void stopAllMusic() {
		if(listener != null)
			listener.stopAllMusic();
	}

	/*
	 * Check for room changes; which may lead to view changes, music changes, etc.
	 */
	public void postUpdateAgency() {
		// check if player changed room, and if so, did the room music change?
	}
}
