package kidridicarus.common.agent;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.Agent;
import kidridicarus.common.agent.optional.PlayerAgent;
import kidridicarus.common.agent.roombox.RoomBox;

public abstract class GameAgentObserver extends AgentObserver {
	public interface AgentObserverListener {
		public void roomMusicUpdate(String musicName);
		public void startSinglePlayMusic(String musicName);
		public void stopAllMusic();
	}
	private AgentObserverListener listener;

	private Vector2 lastViewCenter;

	public GameAgentObserver(Agent agent) {
		super(agent);
		lastViewCenter = new Vector2(0f, 0f);
	}

	public void setListener(AgentObserverListener listener) {
		this.listener = listener;
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

	@Override
	public void roomChange(RoomBox newRoom) {
		if(newRoom != null)
			listener.roomMusicUpdate(newRoom.getRoommusic());
	}

	@Override
	public void startSinglePlayMusic(String musicName) {
		listener.startSinglePlayMusic(musicName);
	}

	@Override
	public void stopAllMusic() {
		listener.stopAllMusic();
	}
}
