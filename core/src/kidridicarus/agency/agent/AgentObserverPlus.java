package kidridicarus.agency.agent;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.agent.general.Room;
import kidridicarus.agency.agent.optional.PlayerAgent;
import kidridicarus.game.tool.QQ;

public abstract class AgentObserverPlus extends AgentObserver {
	public interface AgentObserverListener {
		public void roomMusicUpdate(String musicName);
		public void startSinglePlayMusic(String musicName);
		public void stopAllMusic();
	}
	private AgentObserverListener listener;

	public AgentObserverPlus(Agent agent) {
		super(agent);
	}

	public void setListener(AgentObserverListener listener) {
		this.listener = listener;
	}

	public Vector2 getViewCenter() {
		Room room = ((PlayerAgent) playerAgent).getCurrentRoom();
		if(room == null)
			return new Vector2(0f, 0f);
		return ((PlayerAgent) playerAgent).getCurrentRoom().getViewCenterForPos(playerAgent.getPosition());
	}

	@Override
	public void roomChange(Room newRoom) {
QQ.pr("room change, newRoom="+newRoom);
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
